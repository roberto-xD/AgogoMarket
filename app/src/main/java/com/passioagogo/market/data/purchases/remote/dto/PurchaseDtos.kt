package com.passioagogo.market.data.purchases.remote.dto

import com.passioagogo.market.domain.common.PurchaseStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ 07 · suppliers ============

@Serializable
data class SupplierDto(
    val id: String,
    val nombre: String,
    val contacto: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val notas: String? = null,
    val activo: Boolean = true,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class NewSupplierDto(
    val nombre: String,
    val contacto: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val notas: String? = null,
)

// ============ 07 · purchases ============

@Serializable
data class PurchaseDto(
    val id: String,
    val folio: Long,
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("location_id") val locationId: String,
    val estado: PurchaseStatus = PurchaseStatus.PENDIENTE,
    @SerialName("fecha_pedido") val fechaPedido: String? = null,
    @SerialName("fecha_recepcion") val fechaRecepcion: String? = null,
    val total: Double = 0.0,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("purchase_items") val items: List<PurchaseItemDto> = emptyList(),
)

@Serializable
data class PurchaseItemDto(
    val id: String,
    @SerialName("purchase_id") val purchaseId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
    @SerialName("costo_unitario") val costoUnitario: Double,
)

@Serializable
data class NewPurchaseDto(
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("location_id") val locationId: String,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class NewPurchaseItemDto(
    @SerialName("purchase_id") val purchaseId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
    @SerialName("costo_unitario") val costoUnitario: Double,
)

// ============ 11 · v_historial_costos (solo lectura) ============

@Serializable
data class HistorialCostoDto(
    @SerialName("variant_id") val variantId: String,
    val sku: String,
    val producto: String,
    val proveedor: String,
    @SerialName("folio_compra") val folioCompra: Long,
    @SerialName("fecha_recepcion") val fechaRecepcion: String? = null,
    val cantidad: Int,
    @SerialName("costo_unitario") val costoUnitario: Double,
)
