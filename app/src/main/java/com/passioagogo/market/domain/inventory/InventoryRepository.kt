package com.passioagogo.market.domain.inventory

import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.LocationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.passioagogo.market.domain.common.TransferStatus
import kotlinx.serialization.json.JsonObject

// ============ Modelos ============

data class Location(
    val id: String,
    val nombre: String,
    val direccion: String?,
    val tipo: LocationType,
    val activo: Boolean,
)

data class LocationDraft(
    val nombre: String,
    val direccion: String? = null,
    val tipo: LocationType = LocationType.TIENDA,
)

/** Por qué se ajustó una existencia. Acotado para poder agrupar la bitácora. */
@Serializable
enum class MotivoAjuste {
    @SerialName("conteo") CONTEO,
    @SerialName("alta_inicial") ALTA_INICIAL,
    @SerialName("merma") MERMA,
    @SerialName("dano") DANO,
    @SerialName("robo") ROBO,
    @SerialName("devolucion") DEVOLUCION,
    @SerialName("correccion") CORRECCION,
    @SerialName("otro") OTRO,
}

/** Fila de v_ajustes_inventario. */
data class AjusteInventario(
    val id: String,
    val createdAt: String?,
    val ubicacion: String,
    val producto: String,
    val sku: String,
    val cantidadAnterior: Int,
    val cantidadNueva: Int,
    val diferencia: Int,
    val motivo: MotivoAjuste,
    val nota: String?,
    val responsable: String?,
)

/** Fila de v_stock_actual: existencia con contexto para mostrar. */
data class StockItem(
    val locationId: String,
    val tienda: String,
    val variantId: String,
    val sku: String,
    /** Título de la variante; null = se identifica por sus atributos. */
    val varianteTitulo: String?,
    val producto: String,
    val categoria: String,
    val variante: JsonObject,
    val cantidad: Int,
    val costo: Double,
    val precioVenta: Double,
    /** cantidad × costo. */
    val costoInventario: Double,
    /** cantidad × precio de lista, sin descuentos. */
    val valorVenta: Double,
)

data class StockTransfer(
    val id: String,
    val fromLocationId: String,
    val toLocationId: String,
    val estado: TransferStatus,
    val fechaEnvio: String?,
    val fechaRecepcion: String?,
    val notas: String?,
    val createdBy: String,
    val createdAt: String?,
    val items: List<TransferItem>,
)

data class TransferItem(
    val id: String,
    val variantId: String,
    val cantidad: Int,
)

data class TransferDraft(
    val fromLocationId: String,
    val toLocationId: String,
    val notas: String? = null,
    /** variantId → cantidad */
    val items: Map<String, Int>,
)

// ============ Repositorios ============

/**
 * Ubicaciones: lectura staff, escritura admin (RLS decide).
 * Sin caché: tabla pequeña, siempre remota.
 */
interface LocationRepository {
    suspend fun getLocations(includeInactive: Boolean = false): DataResult<List<Location>>
    suspend fun createLocation(draft: LocationDraft): DataResult<Location>
    suspend fun updateLocation(location: Location): DataResult<Location>
}

/**
 * Stock y transferencias. SIN caché por diseño: un stock viejo en el
 * punto de venta provoca sobreventa — cada consulta va a Supabase.
 * Los movimientos de stock los ejecutan los triggers del servidor;
 * aquí solo se disparan las transiciones de estado.
 */
interface InventoryRepository {

    /** Existencias desde v_stock_actual, opcionalmente filtradas. */
    suspend fun getStock(
        locationId: String? = null,
        variantId: String? = null,
    ): DataResult<List<StockItem>>

    /** Cantidad puntual variante+ubicación (validación rápida en POS). */
    suspend fun getQuantity(variantId: String, locationId: String): DataResult<Int>

    /**
     * Fija la existencia y deja constancia en la bitácora, en una sola
     * transacción del servidor. El admin ajusta cualquier ubicación; el
     * vendedor, solo la suya.
     */
    suspend fun ajustarExistencia(
        variantId: String,
        locationId: String,
        cantidad: Int,
        motivo: MotivoAjuste,
        nota: String? = null,
    ): DataResult<Unit>

    /** Bitácora de ajustes; RLS acota lo que ve cada rol. */
    suspend fun getAjustes(
        locationId: String? = null,
        limite: Int = 100,
    ): DataResult<List<AjusteInventario>>

    // -- Transferencias --

    /**
     * [locationId] limita a las transferencias que involucran esa ubicación,
     * como origen o como destino. El vendedor solo debe ver las suyas: RLS
     * permite a todo el staff leerlas, así que el recorte es del cliente.
     */
    suspend fun getTransfers(
        openOnly: Boolean = true,
        locationId: String? = null,
    ): DataResult<List<StockTransfer>>
    suspend fun getTransfer(id: String): DataResult<StockTransfer>

    /** Crea la transferencia en 'pendiente' con sus artículos. */
    suspend fun createTransfer(draft: TransferDraft): DataResult<StockTransfer>

    /** Agrega o reemplaza un artículo (solo en 'pendiente'; el trigger bloquea el resto). */
    suspend fun upsertTransferItem(transferId: String, variantId: String, cantidad: Int): DataResult<Unit>
    suspend fun removeTransferItem(transferId: String, variantId: String): DataResult<Unit>

    /** pendiente → en_transito: descuenta stock del origen (trigger). */
    suspend fun sendTransfer(id: String): DataResult<StockTransfer>

    /** en_transito → recibida: suma stock al destino (trigger). */
    suspend fun receiveTransfer(id: String): DataResult<StockTransfer>

    /** Cancela; si estaba en tránsito el trigger devuelve el stock al origen. */
    suspend fun cancelTransfer(id: String): DataResult<StockTransfer>
}
