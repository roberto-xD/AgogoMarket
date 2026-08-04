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
            val created = safeSupabaseCall {
                postgrest.from(ORDERS).insert(
                    NewOrderDto(
                        locationId = request.locationId,
                        customerId = request.customerId,
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

            // 3) Descuento manual a nivel pedido (antes de confirmar)
            if (request.descuentoPedido > 0.0) {
                val discounted = safeSupabaseCall {
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
            val confirmed = safeSupabaseCall {
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
            val paid = safeSupabaseCall {
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
}
