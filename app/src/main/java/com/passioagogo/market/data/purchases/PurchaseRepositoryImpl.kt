package com.passioagogo.market.data.purchases

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.purchases.remote.dto.HistorialCostoDto
import com.passioagogo.market.data.purchases.remote.dto.NewPurchaseDto
import com.passioagogo.market.data.purchases.remote.dto.NewPurchaseItemDto
import com.passioagogo.market.data.purchases.remote.dto.NewSupplierDto
import com.passioagogo.market.data.purchases.remote.dto.PurchaseDto
import com.passioagogo.market.data.purchases.remote.dto.SupplierDto
import com.passioagogo.market.domain.common.PurchaseStatus
import com.passioagogo.market.domain.purchases.HistorialCosto
import com.passioagogo.market.domain.purchases.Purchase
import com.passioagogo.market.domain.purchases.PurchaseDraft
import com.passioagogo.market.domain.purchases.PurchaseItem
import com.passioagogo.market.domain.purchases.PurchaseRepository
import com.passioagogo.market.domain.purchases.Supplier
import com.passioagogo.market.domain.purchases.SupplierDraft
import com.passioagogo.market.domain.purchases.SupplierRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private fun SupplierDto.toDomain() = Supplier(
    id = id, nombre = nombre, contacto = contacto, telefono = telefono,
    email = email, direccion = direccion, notas = notas, activo = activo,
)

private fun PurchaseDto.toDomain() = Purchase(
    id = id, folio = folio, supplierId = supplierId, locationId = locationId,
    estado = estado, fechaPedido = fechaPedido, fechaRecepcion = fechaRecepcion,
    total = total, notas = notas, createdBy = createdBy,
    items = items.map {
        PurchaseItem(
            id = it.id, variantId = it.variantId,
            cantidad = it.cantidad, costoUnitario = it.costoUnitario,
        )
    },
)

private fun HistorialCostoDto.toDomain() = HistorialCosto(
    variantId = variantId, sku = sku, producto = producto, proveedor = proveedor,
    folioCompra = folioCompra, fechaRecepcion = fechaRecepcion,
    cantidad = cantidad, costoUnitario = costoUnitario,
)

@Singleton
class SupplierRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SupplierRepository {

    private companion object { const val TABLE = "suppliers" }

    override suspend fun getSuppliers(includeInactive: Boolean): DataResult<List<Supplier>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).select {
                    if (!includeInactive) filter { eq("activo", true) }
                    order("nombre", Order.ASCENDING)
                }.decodeList<SupplierDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun createSupplier(draft: SupplierDraft): DataResult<Supplier> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).insert(
                    NewSupplierDto(
                        nombre = draft.nombre, contacto = draft.contacto,
                        telefono = draft.telefono, email = draft.email,
                        direccion = draft.direccion, notas = draft.notas,
                    )
                ) { select() }.decodeSingle<SupplierDto>()
            }.map { it.toDomain() }
        }

    override suspend fun updateSupplier(supplier: Supplier): DataResult<Supplier> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).update({
                    set("nombre", supplier.nombre)
                    set("contacto", supplier.contacto)
                    set("telefono", supplier.telefono)
                    set("email", supplier.email)
                    set("direccion", supplier.direccion)
                    set("notas", supplier.notas)
                    set("activo", supplier.activo)
                }) {
                    select()
                    filter { eq("id", supplier.id) }
                }.decodeSingle<SupplierDto>()
            }.map { it.toDomain() }
        }
}

@Singleton
class PurchaseRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PurchaseRepository {

    private companion object {
        const val PURCHASES = "purchases"
        const val ITEMS = "purchase_items"
        const val COSTOS_VIEW = "v_historial_costos"
        val PURCHASE_COLUMNS = Columns.raw("*, purchase_items(*)")
    }

    override suspend fun getPurchases(pendingOnly: Boolean): DataResult<List<Purchase>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PURCHASES).select(PURCHASE_COLUMNS) {
                    if (pendingOnly) filter { eq("estado", "pendiente") }
                    order("created_at", Order.DESCENDING)
                }.decodeList<PurchaseDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getPurchase(id: String): DataResult<Purchase> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(PURCHASES).select(PURCHASE_COLUMNS) {
                filter { eq("id", id) }
            }.decodeSingle<PurchaseDto>()
        }.map { it.toDomain() }
    }

    override suspend fun createPurchase(draft: PurchaseDraft): DataResult<Purchase> =
        withContext(io) {
            val userId = auth.currentUserOrNull()?.id
                ?: return@withContext DataResult.Error(DataError.Unauthorized)
            if (draft.items.isEmpty()) {
                return@withContext DataResult.Error(
                    DataError.Business("Una compra requiere al menos un artículo")
                )
            }

            val created = safeSupabaseCall {
                postgrest.from(PURCHASES).insert(
                    NewPurchaseDto(
                        supplierId = draft.supplierId,
                        locationId = draft.locationId,
                        notas = draft.notas,
                        createdBy = userId,
                    )
                ) { select() }.decodeSingle<PurchaseDto>()
            }
            val purchase = when (created) {
                is DataResult.Error -> return@withContext created
                is DataResult.Success -> created.data
            }

            // Si falla, la compra queda 'pendiente' y editable: sin stock movido
            val itemsResult = safeSupabaseCall {
                postgrest.from(ITEMS).insert(
                    draft.items.map { (variantId, line) ->
                        NewPurchaseItemDto(
                            purchaseId = purchase.id,
                            variantId = variantId,
                            cantidad = line.cantidad,
                            costoUnitario = line.costoUnitario,
                        )
                    }
                )
            }
            when (itemsResult) {
                is DataResult.Error -> itemsResult
                is DataResult.Success -> getPurchase(purchase.id)
            }
        }

    override suspend fun upsertPurchaseItem(
        purchaseId: String,
        variantId: String,
        cantidad: Int,
        costoUnitario: Double,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ITEMS).upsert(
                NewPurchaseItemDto(purchaseId, variantId, cantidad, costoUnitario)
            ) { onConflict = "purchase_id,variant_id" }
            Unit
        }
    }

    override suspend fun removePurchaseItem(
        purchaseId: String,
        variantId: String,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ITEMS).delete {
                filter {
                    eq("purchase_id", purchaseId)
                    eq("variant_id", variantId)
                }
            }
            Unit
        }
    }

    override suspend fun receivePurchase(id: String): DataResult<Purchase> =
        changeStatus(id, PurchaseStatus.RECIBIDA)

    override suspend fun cancelPurchase(id: String): DataResult<Purchase> =
        changeStatus(id, PurchaseStatus.CANCELADA)

    override suspend fun getHistorialCostos(variantId: String?): DataResult<List<HistorialCosto>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(COSTOS_VIEW).select {
                    filter { variantId?.let { eq("variant_id", it) } }
                }.decodeList<HistorialCostoDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    private suspend fun changeStatus(id: String, status: PurchaseStatus): DataResult<Purchase> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PURCHASES).update({ set("estado", status) }) {
                    select(PURCHASE_COLUMNS)
                    filter { eq("id", id) }
                }.decodeSingle<PurchaseDto>()
            }.map { it.toDomain() }
        }
}
