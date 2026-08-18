package com.passioagogo.market.data.requests

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.domain.common.RequestStatus
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ Dominio ============

data class OrderRequest(
    val id: String,
    val folio: Long,
    val estado: RequestStatus,
    val clienteNombre: String,
    val clienteTelefono: String?,
    val clienteEmail: String?,
    val notas: String?,
    /** Informativo: al convertir se recotiza contra las promos vigentes. */
    val totalEstimado: Double,
    val createdBy: String,
    val orderId: String?,
    val motivoRechazo: String?,
    val createdAt: String?,
    val items: List<OrderRequestItem>,
)

data class OrderRequestItem(
    val id: String,
    val variantId: String,
    val cantidad: Int,
    val precioEstimado: Double,
)

data class RequestLine(
    val variantId: String,
    val cantidad: Int,
    val precioEstimado: Double,
)

data class OrderRequestDraft(
    val clienteNombre: String,
    val clienteTelefono: String?,
    val clienteEmail: String?,
    val notas: String?,
    val lines: List<RequestLine>,
)

// ============ DTOs ============

@Serializable
data class OrderRequestDto(
    val id: String,
    val folio: Long,
    val estado: RequestStatus = RequestStatus.ENVIADA,
    @SerialName("cliente_nombre") val clienteNombre: String,
    @SerialName("cliente_telefono") val clienteTelefono: String? = null,
    @SerialName("cliente_email") val clienteEmail: String? = null,
    val notas: String? = null,
    @SerialName("total_estimado") val totalEstimado: Double = 0.0,
    @SerialName("created_by") val createdBy: String,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("motivo_rechazo") val motivoRechazo: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("order_request_items") val items: List<OrderRequestItemDto> = emptyList(),
) {
    fun toDomain() = OrderRequest(
        id = id, folio = folio, estado = estado, clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono, clienteEmail = clienteEmail, notas = notas,
        totalEstimado = totalEstimado, createdBy = createdBy, orderId = orderId,
        motivoRechazo = motivoRechazo, createdAt = createdAt,
        items = items.map {
            OrderRequestItem(
                id = it.id, variantId = it.variantId,
                cantidad = it.cantidad, precioEstimado = it.precioEstimado,
            )
        },
    )
}

@Serializable
data class OrderRequestItemDto(
    val id: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
    @SerialName("precio_estimado") val precioEstimado: Double = 0.0,
)

@Serializable
data class NewOrderRequestDto(
    @SerialName("cliente_nombre") val clienteNombre: String,
    @SerialName("cliente_telefono") val clienteTelefono: String? = null,
    @SerialName("cliente_email") val clienteEmail: String? = null,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class NewOrderRequestItemDto(
    @SerialName("request_id") val requestId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
    @SerialName("precio_estimado") val precioEstimado: Double,
)

// ============ Repositorio ============

/**
 * Solicitudes de pedido levantadas por promotores. No mueven inventario:
 * un admin las convierte en pedido real, y es ahí donde se descuenta stock
 * y se recotizan los precios.
 */
interface OrderRequestRepository {
    /** RLS decide el alcance: el promotor ve las suyas, el admin todas. */
    suspend fun getRequests(pendingOnly: Boolean = false): DataResult<List<OrderRequest>>
    suspend fun getRequest(id: String): DataResult<OrderRequest>
    suspend fun createRequest(draft: OrderRequestDraft): DataResult<OrderRequest>
    suspend fun deleteRequest(id: String): DataResult<Unit>

    /** Marca la solicitud como atendida y la enlaza con el pedido creado. */
    suspend fun markAttended(id: String, orderId: String): DataResult<Unit>
    suspend fun reject(id: String, motivo: String): DataResult<Unit>
}

@Singleton
class OrderRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) : OrderRequestRepository {

    private companion object {
        const val REQUESTS = "order_requests"
        const val ITEMS = "order_request_items"
        val REQUEST_COLUMNS = Columns.raw("*, order_request_items(*)")
    }

    override suspend fun getRequests(pendingOnly: Boolean): DataResult<List<OrderRequest>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(REQUESTS).select(REQUEST_COLUMNS) {
                    if (pendingOnly) filter { eq("estado", "enviada") }
                    order("created_at", Order.DESCENDING)
                    limit(100)
                }.decodeList<OrderRequestDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getRequest(id: String): DataResult<OrderRequest> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(REQUESTS).select(REQUEST_COLUMNS) {
                filter { eq("id", id) }
            }.decodeSingle<OrderRequestDto>()
        }.map { it.toDomain() }
    }

    override suspend fun createRequest(draft: OrderRequestDraft): DataResult<OrderRequest> =
        withContext(io) {
            val userId = auth.currentUserOrNull()?.id
                ?: return@withContext DataResult.Error(DataError.Unauthorized)
            if (draft.lines.isEmpty()) {
                return@withContext DataResult.Error(
                    DataError.Business("La solicitud no tiene artículos")
                )
            }

            val created = safeSupabaseCall {
                postgrest.from(REQUESTS).insert(
                    NewOrderRequestDto(
                        clienteNombre = draft.clienteNombre,
                        clienteTelefono = draft.clienteTelefono,
                        clienteEmail = draft.clienteEmail,
                        notas = draft.notas,
                        createdBy = userId,
                    )
                ) { select() }.decodeSingle<OrderRequestDto>()
            }
            val request = when (created) {
                is DataResult.Error -> return@withContext created
                is DataResult.Success -> created.data
            }

            // Si fallan los artículos, la cabecera queda 'enviada' y editable
            val itemsResult = safeSupabaseCall {
                postgrest.from(ITEMS).insert(
                    draft.lines.map {
                        NewOrderRequestItemDto(
                            requestId = request.id,
                            variantId = it.variantId,
                            cantidad = it.cantidad,
                            precioEstimado = it.precioEstimado,
                        )
                    }
                )
            }
            when (itemsResult) {
                is DataResult.Error -> itemsResult
                is DataResult.Success -> getRequest(request.id)
            }
        }

    override suspend fun deleteRequest(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(REQUESTS).delete { filter { eq("id", id) } }
            Unit
        }
    }

    override suspend fun markAttended(id: String, orderId: String): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(REQUESTS).update({
                    set("estado", RequestStatus.ATENDIDA)
                    set("order_id", orderId)
                }) { filter { eq("id", id) } }
                Unit
            }
        }

    override suspend fun reject(id: String, motivo: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(REQUESTS).update({
                set("estado", RequestStatus.RECHAZADA)
                set("motivo_rechazo", motivo)
            }) { filter { eq("id", id) } }
            Unit
        }
    }
}
