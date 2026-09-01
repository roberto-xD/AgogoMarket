package com.passioagogo.market.data.transferrequests

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ============ Enums ============

@Serializable
enum class TransferRequestType {
    /** El destino pide mercancía; autoriza el origen. */
    @SerialName("pedido") PEDIDO,
    /** El origen pide devolver; autoriza el destino. */
    @SerialName("devolucion") DEVOLUCION,
}

@Serializable
enum class TransferRequestStatus {
    @SerialName("solicitada") SOLICITADA,
    @SerialName("aceptada") ACEPTADA,
    @SerialName("rechazada") RECHAZADA,
    @SerialName("cancelada") CANCELADA,
}

// ============ Dominio ============

data class TransferRequest(
    val id: String,
    val folio: Long,
    val tipo: TransferRequestType,
    val estado: TransferRequestStatus,
    /** null en un pedido con origen abierto: lo decide el admin. */
    val fromLocationId: String?,
    val toLocationId: String,
    val notas: String?,
    val motivoRechazo: String?,
    val solicitadoPor: String,
    val solicitanteNombre: String?,
    val transferId: String?,
    val createdAt: String?,
    val items: List<TransferRequestItem>,
)

data class TransferRequestItem(
    val id: String,
    val variantId: String,
    val cantidadSolicitada: Int,
    /** Lo que realmente se autorizó; null mientras esté abierta. */
    val cantidadAprobada: Int?,
)

data class TransferRequestDraft(
    val tipo: TransferRequestType,
    val fromLocationId: String?,
    val toLocationId: String,
    val notas: String?,
    /** variantId → cantidad solicitada */
    val items: Map<String, Int>,
)

/** Fila de v_disponibilidad_variante: dónde hay stock de una variante. */
data class Disponibilidad(
    val variantId: String,
    val locationId: String,
    val ubicacion: String,
    val cantidad: Int,
)

// ============ DTOs ============

@Serializable
data class TransferRequestDto(
    val id: String,
    val folio: Long,
    val tipo: TransferRequestType,
    val estado: TransferRequestStatus = TransferRequestStatus.SOLICITADA,
    @SerialName("from_location_id") val fromLocationId: String? = null,
    @SerialName("to_location_id") val toLocationId: String,
    val notas: String? = null,
    @SerialName("motivo_rechazo") val motivoRechazo: String? = null,
    @SerialName("solicitado_por") val solicitadoPor: String,
    /** Embebido con alias: hay dos FK a profiles (solicitado_por, resuelto_por). */
    val solicitante: PerfilNombreDto? = null,
    @SerialName("transfer_id") val transferId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("transfer_request_items") val items: List<TransferRequestItemDto> = emptyList(),
) {
    fun toDomain() = TransferRequest(
        id = id, folio = folio, tipo = tipo, estado = estado,
        fromLocationId = fromLocationId, toLocationId = toLocationId,
        notas = notas, motivoRechazo = motivoRechazo,
        solicitadoPor = solicitadoPor, solicitanteNombre = solicitante?.nombre,
        transferId = transferId, createdAt = createdAt,
        items = items.map {
            TransferRequestItem(
                id = it.id,
                variantId = it.variantId,
                cantidadSolicitada = it.cantidadSolicitada,
                cantidadAprobada = it.cantidadAprobada,
            )
        },
    )
}

@Serializable
data class PerfilNombreDto(val nombre: String)

@Serializable
data class TransferRequestItemDto(
    val id: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("variant_id") val variantId: String,
    @SerialName("cantidad_solicitada") val cantidadSolicitada: Int,
    @SerialName("cantidad_aprobada") val cantidadAprobada: Int? = null,
)

@Serializable
data class NewTransferRequestDto(
    val tipo: TransferRequestType,
    @SerialName("from_location_id") val fromLocationId: String? = null,
    @SerialName("to_location_id") val toLocationId: String,
    val notas: String? = null,
    @SerialName("solicitado_por") val solicitadoPor: String,
)

@Serializable
data class NewTransferRequestItemDto(
    @SerialName("request_id") val requestId: String,
    @SerialName("variant_id") val variantId: String,
    @SerialName("cantidad_solicitada") val cantidadSolicitada: Int,
)

@Serializable
data class DisponibilidadDto(
    @SerialName("variant_id") val variantId: String,
    @SerialName("location_id") val locationId: String,
    val ubicacion: String,
    val cantidad: Int,
)

// ============ Repositorio ============

/**
 * Solicitudes de transferencia. No mueven inventario: al aceptarlas, el
 * RPC del servidor crea la `stock_transfers` real y a partir de ahí sigue
 * el flujo de envío y recepción que ya existía.
 */
interface TransferRequestRepository {
    /** RLS decide el alcance: cada quien ve las de su ubicación. */
    suspend fun getRequests(openOnly: Boolean = true): DataResult<List<TransferRequest>>
    suspend fun getRequest(id: String): DataResult<TransferRequest>
    suspend fun createRequest(draft: TransferRequestDraft): DataResult<TransferRequest>

    /**
     * Acepta y genera la transferencia.
     * [aprobadas] permite recortar cantidades; null acepta todo lo pedido.
     * [fromLocationId] solo hace falta si el pedido dejó el origen abierto.
     */
    suspend fun aceptar(
        id: String,
        fromLocationId: String? = null,
        aprobadas: Map<String, Int>? = null,
    ): DataResult<String>

    suspend fun rechazar(id: String, motivo: String): DataResult<Unit>
    suspend fun cancelar(id: String): DataResult<Unit>

    /** Dónde hay existencia de las variantes indicadas. */
    suspend fun getDisponibilidad(variantIds: List<String>): DataResult<List<Disponibilidad>>
}

@Singleton
class TransferRequestRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) : TransferRequestRepository {

    private companion object {
        const val REQUESTS = "transfer_requests"
        const val ITEMS = "transfer_request_items"
        const val DISPONIBILIDAD = "v_disponibilidad_variante"
        const val RPC_ACEPTAR = "fn_aceptar_solicitud_transferencia"
        val REQUEST_COLUMNS = Columns.raw(
            "*, transfer_request_items(*), solicitante:profiles!solicitado_por(nombre)",
        )
    }

    override suspend fun getRequests(openOnly: Boolean): DataResult<List<TransferRequest>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(REQUESTS).select(REQUEST_COLUMNS) {
                    if (openOnly) filter { eq("estado", "solicitada") }
                    order("created_at", Order.DESCENDING)
                    limit(100)
                }.decodeList<TransferRequestDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getRequest(id: String): DataResult<TransferRequest> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(REQUESTS).select(REQUEST_COLUMNS) {
                filter { eq("id", id) }
            }.decodeSingle<TransferRequestDto>()
        }.map { it.toDomain() }
    }

    override suspend fun createRequest(
        draft: TransferRequestDraft,
    ): DataResult<TransferRequest> = withContext(io) {
        val userId = auth.currentUserOrNull()?.id
            ?: return@withContext DataResult.Error(DataError.Unauthorized)
        if (draft.items.isEmpty()) {
            return@withContext DataResult.Error(
                DataError.Business("La solicitud no tiene artículos")
            )
        }

        val creada = safeSupabaseCall {
            postgrest.from(REQUESTS).insert(
                NewTransferRequestDto(
                    tipo = draft.tipo,
                    fromLocationId = draft.fromLocationId,
                    toLocationId = draft.toLocationId,
                    notas = draft.notas,
                    solicitadoPor = userId,
                )
            ) { select() }.decodeSingle<TransferRequestDto>()
        }
        val request = when (creada) {
            is DataResult.Error -> return@withContext creada
            is DataResult.Success -> creada.data
        }

        // Si fallan los artículos la cabecera queda abierta y editable
        val itemsResult = safeSupabaseCall {
            postgrest.from(ITEMS).insert(
                draft.items.map { (variantId, cantidad) ->
                    NewTransferRequestItemDto(
                        requestId = request.id,
                        variantId = variantId,
                        cantidadSolicitada = cantidad,
                    )
                }
            )
        }
        when (itemsResult) {
            is DataResult.Error -> itemsResult
            is DataResult.Success -> getRequest(request.id)
        }
    }

    override suspend fun aceptar(
        id: String,
        fromLocationId: String?,
        aprobadas: Map<String, Int>?,
    ): DataResult<String> = withContext(io) {
        safeSupabaseCall {
            val parametros = buildJsonObject {
                put("p_request", id)
                fromLocationId?.let { put("p_from", it) }
                aprobadas?.let { mapa ->
                    put("p_items", construirItems(mapa))
                }
            }
            // El RPC devuelve el uuid de la transferencia creada
            postgrest.rpc(RPC_ACEPTAR, parametros)
                .data.trim().trim('"')
        }
    }

    private fun construirItems(aprobadas: Map<String, Int>): JsonArray = buildJsonArray {
        aprobadas.forEach { (variantId, cantidad) ->
            add(
                buildJsonObject {
                    put("variant_id", variantId)
                    put("cantidad", cantidad)
                }
            )
        }
    }

    override suspend fun rechazar(id: String, motivo: String): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(REQUESTS).update({
                    set("estado", TransferRequestStatus.RECHAZADA)
                    set("motivo_rechazo", motivo)
                }) { filter { eq("id", id) } }
                Unit
            }
        }

    override suspend fun cancelar(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(REQUESTS).update({
                set("estado", TransferRequestStatus.CANCELADA)
            }) { filter { eq("id", id) } }
            Unit
        }
    }

    override suspend fun getDisponibilidad(
        variantIds: List<String>,
    ): DataResult<List<Disponibilidad>> = withContext(io) {
        if (variantIds.isEmpty()) return@withContext DataResult.Success(emptyList())
        safeSupabaseCall {
            postgrest.from(DISPONIBILIDAD).select {
                filter { isIn("variant_id", variantIds) }
            }.decodeList<DisponibilidadDto>()
        }.map { list ->
            list.map {
                Disponibilidad(
                    variantId = it.variantId,
                    locationId = it.locationId,
                    ubicacion = it.ubicacion,
                    cantidad = it.cantidad,
                )
            }
        }
    }
}
