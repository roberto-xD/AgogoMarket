package com.passioagogo.market.data.sales

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.sales.remote.dto.NewOrderDto
import com.passioagogo.market.data.sales.remote.dto.NewPaymentDto
import com.passioagogo.market.data.sales.remote.dto.OrderDto
import com.passioagogo.market.data.sales.remote.dto.PrecioVigenteDto
import com.passioagogo.market.domain.common.OrderStatus
import com.passioagogo.market.domain.common.OrderType
import com.passioagogo.market.domain.common.PaymentMethod
import com.passioagogo.market.domain.sales.ShippingOrderDraft
import com.passioagogo.market.domain.sales.CheckoutRequest
import com.passioagogo.market.domain.sales.Order
import com.passioagogo.market.domain.sales.OrderItem
import com.passioagogo.market.domain.sales.Payment
import com.passioagogo.market.domain.sales.PrecioVigente
import com.passioagogo.market.domain.sales.SalesRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SortOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private fun OrderDto.toDomain() = Order(
    id = id, folio = folio, customerId = customerId, locationId = locationId,
    tipo = tipo, estado = estado, costoEnvio = costoEnvio, subtotal = subtotal,
    descuento = descuento, total = total,
    shippingAddressId = shippingAddressId, numeroGuia = numeroGuia,
    paqueteria = paqueteria, notas = notas, createdBy = createdBy,
    confirmedAt = confirmedAt, createdAt = createdAt,
    items = items.map {
        OrderItem(
            id = it.id, variantId = it.variantId, cantidad = it.cantidad,
            precioUnitario = it.precioUnitario, promotionId = it.promotionId,
            descuento = it.descuento,
        )
    },
    payments = payments.map {
        Payment(
            id = it.id, monto = it.monto, metodo = it.metodo,
            referencia = it.referencia, fecha = it.fecha,
        )
    },
)

private fun PrecioVigenteDto.toDomain() = PrecioVigente(
    variantId = variantId, precioLista = precioLista,
    precioFinal = precioFinal, promotionId = promotionId,
)

@Singleton
class SalesRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SalesRepository {

    private companion object {
        const val ORDERS = "orders"
        const val ORDER_ITEMS = "order_items"
        const val PAYMENTS = "payments"
        const val RPC_PRECIO = "fn_precio_vigente"
        const val RPC_AGREGAR = "fn_agregar_item"
        val ORDER_COLUMNS = Columns.raw("*, order_items(*), payments(*)")
    }

    override suspend fun getPrecioVigente(variantId: String): DataResult<PrecioVigente> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.rpc(
                    RPC_PRECIO,
                    buildJsonObject { put("p_variant", variantId) },
                )
                    .decodeList<PrecioVigenteDto>()
                    .first()
            }.map { it.toDomain() }
        }

    override suspend fun checkout(request: CheckoutRequest): DataResult<Order> =
        withContext(io) {
            val userId = auth.currentUserOrNull()?.id
                ?: return@withContext DataResult.Error(DataError.Unauthorized)
            if (request.lines.isEmpty()) {
                return@withContext DataResult.Error(
                    DataError.Business("El carrito está vacío")
                )
            }

            // 1) Pedido en 'pendiente'
            val created = safeSupabaseCall("checkout.crearPedido") {
                postgrest.from(ORDERS).insert(
                    NewOrderDto(
                        locationId = request.locationId,
                        customerId = request.customerId,
                        // Lo que costó la guía, si la venta vino de una
                        // solicitud con envío ya despachado.
                        costoEnvio = request.costoEnvio,
                        notas = request.notas,
                        createdBy = userId,
                    )
                ) { select() }.decodeSingle<OrderDto>()
            }
            val order = when (created) {
                is DataResult.Error -> return@withContext created
                is DataResult.Success -> created.data
            }

            // 2) Líneas vía RPC: el servidor cotiza y congela precios
            for (line in request.lines) {
                val added = safeSupabaseCall("checkout.agregarItem") {
                    postgrest.rpc(
                        RPC_AGREGAR,
                        buildJsonObject {
                            put("p_order", order.id)
                            put("p_variant", line.variantId)
                            put("p_cantidad", line.cantidad)
                        },
                    )
                }
                if (added is DataResult.Error) {
                    tryCancel(order.id)
                    return@withContext added
                }
            }

            // 3) Descuento manual a nivel pedido (antes de confirmar)
            if (request.descuentoPedido > 0.0) {
                val discounted = safeSupabaseCall("checkout.aplicarDescuento") {
                    postgrest.from(ORDERS).update({
                        set("descuento", request.descuentoPedido)
                    }) { filter { eq("id", order.id) } }
                }
                if (discounted is DataResult.Error) {
                    tryCancel(order.id)
                    return@withContext discounted
                }
            }

            // 4) Confirmar: el trigger valida y descuenta stock
            val confirmed = safeSupabaseCall("checkout.confirmar") {
                postgrest.from(ORDERS).update({
                    set("estado", OrderStatus.CONFIRMADO)
                }) {
                    select(ORDER_COLUMNS)
                    filter { eq("id", order.id) }
                }.decodeSingle<OrderDto>()
            }
            val confirmedOrder = when (confirmed) {
                is DataResult.Error -> {
                    tryCancel(order.id)
                    return@withContext confirmed
                }
                is DataResult.Success -> confirmed.data
            }

            // 5) Pago por el total (calculado por el servidor)
            val paid = safeSupabaseCall("checkout.registrarPago") {
                postgrest.from(PAYMENTS).insert(
                    NewPaymentDto(
                        orderId = confirmedOrder.id,
                        monto = confirmedOrder.total,
                        metodo = request.metodo,
                        referencia = request.referencia,
                        receivedBy = userId,
                    )
                )
            }
            if (paid is DataResult.Error) {
                // El pedido ya está confirmado y el stock descontado: NO se
                // cancela automáticamente. Queda en v_saldos_pendientes.
                return@withContext DataResult.Error(
                    DataError.Business(
                        "La venta #${confirmedOrder.folio} se confirmó pero el pago " +
                            "no se registró. Regístralo desde saldos pendientes."
                    )
                )
            }

            getOrder(confirmedOrder.id)
        }

    override suspend fun getOrder(id: String): DataResult<Order> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ORDERS).select(ORDER_COLUMNS) {
                filter { eq("id", id) }
            }.decodeSingle<OrderDto>()
        }.map { it.toDomain() }
    }

    override suspend fun getRecentOrders(
        locationId: String?,
        limit: Int,
    ): DataResult<List<Order>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ORDERS).select(ORDER_COLUMNS) {
                filter { locationId?.let { eq("location_id", it) } }
                order("created_at", SortOrder.DESCENDING)
                limit(limit.toLong())
            }.decodeList<OrderDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun cancelOrder(id: String): DataResult<Order> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ORDERS).update({
                set("estado", OrderStatus.CANCELADO)
            }) {
                select(ORDER_COLUMNS)
                filter { eq("id", id) }
            }.decodeSingle<OrderDto>()
        }.map { it.toDomain() }
    }

    override suspend fun createShippingOrder(draft: ShippingOrderDraft): DataResult<Order> =
        withContext(io) {
            val userId = auth.currentUserOrNull()?.id
                ?: return@withContext DataResult.Error(DataError.Unauthorized)
            if (draft.lines.isEmpty()) {
                return@withContext DataResult.Error(DataError.Business("El envío no tiene artículos"))
            }

            // 1) Pedido de envío en 'pendiente'
            val created = safeSupabaseCall {
                postgrest.from(ORDERS).insert(
                    NewOrderDto(
                        locationId = draft.locationId,
                        tipo = OrderType.ENVIO,
                        customerId = draft.customerId,
                        shippingAddressId = draft.shippingAddressId,
                        costoEnvio = draft.costoEnvio,
                        notas = draft.notas,
                        createdBy = userId,
                    )
                ) { select() }.decodeSingle<OrderDto>()
            }
            val order = when (created) {
                is DataResult.Error -> return@withContext created
                is DataResult.Success -> created.data
            }

            // 2) Líneas con precio congelado en servidor
            for (line in draft.lines) {
                val added = safeSupabaseCall {
                    postgrest.rpc(
                        RPC_AGREGAR,
                        buildJsonObject {
                            put("p_order", order.id)
                            put("p_variant", line.variantId)
                            put("p_cantidad", line.cantidad)
                        },
                    )
                }
                if (added is DataResult.Error) {
                    tryCancel(order.id)
                    return@withContext added
                }
            }

            // 3) Confirmar: snapshot de dirección + descuento de stock (triggers)
            val confirmed = safeSupabaseCall {
                postgrest.from(ORDERS).update({
                    set("estado", OrderStatus.CONFIRMADO)
                }) {
                    select(ORDER_COLUMNS)
                    filter { eq("id", order.id) }
                }.decodeSingle<OrderDto>()
            }
            when (confirmed) {
                is DataResult.Error -> {
                    tryCancel(order.id)
                    confirmed
                }
                is DataResult.Success -> DataResult.Success(confirmed.data.toDomain())
            }
        }

    override suspend fun shipOrder(
        id: String,
        paqueteria: String,
        numeroGuia: String,
    ): DataResult<Order> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ORDERS).update({
                set("estado", OrderStatus.EN_TRANSITO)
                set("paqueteria", paqueteria)
                set("numero_guia", numeroGuia)
            }) {
                select(ORDER_COLUMNS)
                filter { eq("id", id) }
            }.decodeSingle<OrderDto>()
        }.map { it.toDomain() }
    }

    override suspend fun deliverOrder(id: String): DataResult<Order> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ORDERS).update({
                set("estado", OrderStatus.ENTREGADO)
            }) {
                select(ORDER_COLUMNS)
                filter { eq("id", id) }
            }.decodeSingle<OrderDto>()
        }.map { it.toDomain() }
    }

    override suspend fun addPayment(
        orderId: String,
        monto: Double,
        metodo: PaymentMethod,
        referencia: String?,
    ): DataResult<Order> = withContext(io) {
        val userId = auth.currentUserOrNull()?.id
            ?: return@withContext DataResult.Error(DataError.Unauthorized)
        val inserted = safeSupabaseCall {
            postgrest.from(PAYMENTS).insert(
                NewPaymentDto(
                    orderId = orderId,
                    monto = monto,
                    metodo = metodo,
                    referencia = referencia?.ifBlank { null },
                    receivedBy = userId,
                )
            )
        }
        when (inserted) {
            is DataResult.Error -> inserted
            is DataResult.Success -> getOrder(orderId)
        }
    }

    /** Limpieza best-effort: cancela el pedido pendiente tras un fallo. */
    private suspend fun tryCancel(orderId: String) {
        safeSupabaseCall {
            postgrest.from(ORDERS).update({
                set("estado", OrderStatus.CANCELADO)
            }) { filter { eq("id", orderId) } }
        }
    }

    // ============ Pedido abierto ============

    override suspend fun crearPedidoAbierto(
        locationId: String,
        lines: List<CartLine>,
    ): DataResult<Order> = withContext(io) {
        val userId = auth.currentUserOrNull()?.id
            ?: return@withContext DataResult.Error(DataError.Unauthorized)
        if (lines.isEmpty()) {
            return@withContext DataResult.Error(
                DataError.Business("El pedido no tiene artículos")
            )
        }

        val created = safeSupabaseCall("crearPedidoAbierto") {
            postgrest.from(ORDERS).insert(
                NewOrderDto(
                    locationId = locationId,
                    tipo = OrderType.ENVIO,
                    createdBy = userId,
                )
            ) { select() }.decodeSingle<OrderDto>()
        }
        val order = when (created) {
            is DataResult.Error -> return@withContext created
            is DataResult.Success -> created.data
        }

        for (line in lines) {
            val added = agregarLinea(order.id, line.variantId, line.cantidad)
            if (added is DataResult.Error) {
                // Sin líneas el pedido no sirve: se descarta en vez de
                // dejar un vacío rondando en la lista de abiertos.
                tryCancel(order.id)
                return@withContext added
            }
        }
        getOrder(order.id)
    }

    override suspend fun getPedidosAbiertos(
        locationId: String?,
        masNuevosPrimero: Boolean,
    ): DataResult<List<Order>> = withContext(io) {
        safeSupabaseCall("getPedidosAbiertos") {
            postgrest.from(ORDERS).select(ORDER_COLUMNS) {
                filter {
                    // .sql y no el enum: en un filtro se serializa con
                    // toString() y PostgreSQL rechaza las mayúsculas.
                    eq("estado", OrderStatus.PENDIENTE.sql)
                    eq("tipo", OrderType.ENVIO.sql)
                    locationId?.let { eq("location_id", it) }
                }
                order(
                    "created_at",
                    if (masNuevosPrimero) SortOrder.DESCENDING else SortOrder.ASCENDING,
                )
                limit(100)
            }.decodeList<OrderDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun agregarLinea(
        orderId: String,
        variantId: String,
        cantidad: Int,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall("agregarLinea") {
            // El servidor recotiza con la promoción del día y congela el
            // precio en la línea: no se calcula nada en el cliente.
            postgrest.rpc(
                RPC_AGREGAR,
                buildJsonObject {
                    put("p_order", orderId)
                    put("p_variant", variantId)
                    put("p_cantidad", cantidad)
                },
            )
            Unit
        }
    }

    override suspend fun quitarLinea(
        orderId: String,
        itemId: String,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall("quitarLinea") {
            postgrest.from(ORDER_ITEMS).delete {
                filter {
                    eq("id", itemId)
                    eq("order_id", orderId)
                }
            }
            Unit
        }
    }

    override suspend fun actualizarDatosPedido(
        orderId: String,
        customerId: String?,
        shippingAddressId: String?,
        costoEnvio: Double?,
        notas: String?,
    ): DataResult<Order> = withContext(io) {
        safeSupabaseCall("actualizarDatosPedido") {
            postgrest.from(ORDERS).update({
                customerId?.let { set("customer_id", it) }
                shippingAddressId?.let { set("shipping_address_id", it) }
                costoEnvio?.let { set("costo_envio", it) }
                notas?.let { set("notas", it) }
            }) {
                select(ORDER_COLUMNS)
                filter { eq("id", orderId) }
            }.decodeSingle<OrderDto>()
        }.map { it.toDomain() }
    }

    override suspend fun confirmarPedido(orderId: String): DataResult<Order> =
        withContext(io) {
            safeSupabaseCall("confirmarPedido") {
                // Aquí es donde los triggers descuentan stock y congelan la
                // dirección. Si no alcanza la existencia, falla y el pedido
                // se queda abierto, que es lo correcto.
                postgrest.from(ORDERS).update({
                    set("estado", OrderStatus.CONFIRMADO)
                }) {
                    select(ORDER_COLUMNS)
                    filter { eq("id", orderId) }
                }.decodeSingle<OrderDto>()
            }.map { it.toDomain() }
        }

    override suspend fun descartarPedido(orderId: String): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall("descartarPedido") {
                postgrest.from(ORDERS).update({
                    set("estado", OrderStatus.CANCELADO)
                }) { filter { eq("id", orderId) } }
                Unit
            }
        }
}
