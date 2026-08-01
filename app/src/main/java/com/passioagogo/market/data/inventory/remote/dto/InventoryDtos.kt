package com.passioagogo.market.data.inventory.remote.dto

import com.passioagogo.market.domain.common.LocationType
import com.passioagogo.market.domain.common.TransferStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// ============ 02 · locations ============

@Serializable
data class LocationDto(
    val id: String,
    val nombre: String,
    val direccion: String? = null,
    val tipo: LocationType = LocationType.TIENDA,
    val activo: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class NewLocationDto(
    val nombre: String,
    val direccion: String? = null,
    val tipo: LocationType = LocationType.TIENDA,
)

// ============ 04 · stock ============

@Serializable
data class StockDto(
    val id: String,
    @SerialName("variant_id") val variantId: String,
    @SerialName("location_id") val locationId: String,
    val cantidad: Int,
    @SerialName("updated_at") val updatedAt: String? = null,
)

/** Upsert manual de stock (solo admin según RLS). */
@Serializable
data class StockUpsertDto(
    @SerialName("variant_id") val variantId: String,
    @SerialName("location_id") val locationId: String,
    val cantidad: Int,
)

// ============ 11 · v_stock_actual (solo lectura) ============

@Serializable
data class StockActualDto(
    @SerialName("location_id") val locationId: String,
    val tienda: String,
    @SerialName("variant_id") val variantId: String,
    val sku: String,
    val producto: String,
    val categoria: String,
    val variante: JsonObject = JsonObject(emptyMap()),
    val cantidad: Int,
    val costo: Double,
    @SerialName("valor_inventario") val valorInventario: Double,
)

// ============ 04 · stock_transfers ============

@Serializable
data class StockTransferDto(
    val id: String,
    @SerialName("from_location_id") val fromLocationId: String,
    @SerialName("to_location_id") val toLocationId: String,
    val estado: TransferStatus = TransferStatus.PENDIENTE,
    @SerialName("fecha_envio") val fechaEnvio: String? = null,
    @SerialName("fecha_recepcion") val fechaRecepcion: String? = null,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
    @SerialName("created_at") val createdAt: String? = null,
    /** Poblado cuando el select incluye el recurso embebido. */
    @SerialName("stock_transfer_items") val items: List<StockTransferItemDto> = emptyList(),
)

@Serializable
data class StockTransferItemDto(
    val id: String,
    @SerialName("transfer_id") val transferId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
)

@Serializable
data class NewStockTransferDto(
    @SerialName("from_location_id") val fromLocationId: String,
    @SerialName("to_location_id") val toLocationId: String,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class NewStockTransferItemDto(
    @SerialName("transfer_id") val transferId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
)
