package com.passioagogo.market.domain.purchases

import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.PurchaseStatus

// ============ Modelos ============

data class Supplier(
    val id: String,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val notas: String?,
    val activo: Boolean,
)

data class SupplierDraft(
    val nombre: String,
    val contacto: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val notas: String? = null,
)

data class Purchase(
    val id: String,
    val folio: Long,
    val supplierId: String,
    val locationId: String,
    val estado: PurchaseStatus,
    val fechaPedido: String?,
    val fechaRecepcion: String?,
    val total: Double,
    val notas: String?,
    val createdBy: String,
    val items: List<PurchaseItem>,
)

data class PurchaseItem(
    val id: String,
    val variantId: String,
    val cantidad: Int,
    val costoUnitario: Double,
)

data class PurchaseDraft(
    val supplierId: String,
    /** Ubicación donde ENTRA la mercancía al recibir. */
    val locationId: String,
    val notas: String? = null,
    /** variantId → (cantidad, costoUnitario) */
    val items: Map<String, PurchaseLine>,
)

data class PurchaseLine(val cantidad: Int, val costoUnitario: Double)

/** Fila de v_historial_costos: comparativa de proveedores. */
data class HistorialCosto(
    val variantId: String,
    val sku: String,
    val producto: String,
    val proveedor: String,
    val folioCompra: Long,
    val fechaRecepcion: String?,
    val cantidad: Int,
    val costoUnitario: Double,
)

// ============ Repositorios ============

/** Proveedores: lectura staff, escritura admin (RLS decide). */
interface SupplierRepository {
    suspend fun getSuppliers(includeInactive: Boolean = false): DataResult<List<Supplier>>
    suspend fun createSupplier(draft: SupplierDraft): DataResult<Supplier>
    suspend fun updateSupplier(supplier: Supplier): DataResult<Supplier>
}

/**
 * Compras a proveedores. Al recibir, los triggers del servidor suman
 * stock en la ubicación destino y propagan el costo a la variante.
 */
interface PurchaseRepository {

    suspend fun getPurchases(pendingOnly: Boolean = false): DataResult<List<Purchase>>
    suspend fun getPurchase(id: String): DataResult<Purchase>

    /** Crea la compra en 'pendiente' con sus artículos. */
    suspend fun createPurchase(draft: PurchaseDraft): DataResult<Purchase>

    /** Solo en 'pendiente'; el trigger bloquea el resto. */
    suspend fun upsertPurchaseItem(
        purchaseId: String,
        variantId: String,
        cantidad: Int,
        costoUnitario: Double,
    ): DataResult<Unit>

    suspend fun removePurchaseItem(purchaseId: String, variantId: String): DataResult<Unit>

    /** pendiente → recibida: suma stock y propaga costo (triggers). */
    suspend fun receivePurchase(id: String): DataResult<Purchase>

    /** pendiente → cancelada. */
    suspend fun cancelPurchase(id: String): DataResult<Purchase>

    /** Historial de costos por variante (todas si null). */
    suspend fun getHistorialCostos(variantId: String? = null): DataResult<List<HistorialCosto>>
}
