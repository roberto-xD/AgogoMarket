package com.passioagogo.market.data.inventory

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.inventory.remote.dto.NewStockTransferDto
import com.passioagogo.market.data.inventory.remote.dto.NewStockTransferItemDto
import com.passioagogo.market.data.inventory.remote.dto.StockActualDto
import com.passioagogo.market.data.inventory.remote.dto.StockDto
import com.passioagogo.market.data.inventory.remote.dto.StockTransferDto
import com.passioagogo.market.data.inventory.remote.dto.StockUpsertDto
import com.passioagogo.market.domain.common.TransferStatus
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.StockItem
import com.passioagogo.market.domain.inventory.StockTransfer
import com.passioagogo.market.domain.inventory.TransferDraft
import com.passioagogo.market.domain.inventory.TransferItem
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private fun StockActualDto.toDomain() = StockItem(
    locationId = locationId, tienda = tienda, variantId = variantId,
    sku = sku, producto = producto, categoria = categoria, variante = variante,
    cantidad = cantidad, costo = costo, valorInventario = valorInventario,
)

private fun StockTransferDto.toDomain() = StockTransfer(
    id = id, fromLocationId = fromLocationId, toLocationId = toLocationId,
    estado = estado, fechaEnvio = fechaEnvio, fechaRecepcion = fechaRecepcion,
    notas = notas, createdBy = createdBy, createdAt = createdAt,
    items = items.map { TransferItem(id = it.id, variantId = it.variantId, cantidad = it.cantidad) },
)

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) : InventoryRepository {

    private companion object {
        const val STOCK = "stock"
        const val STOCK_VIEW = "v_stock_actual"
        const val TRANSFERS = "stock_transfers"
        const val TRANSFER_ITEMS = "stock_transfer_items"
        val TRANSFER_COLUMNS = Columns.raw("*, stock_transfer_items(*)")
    }

    // ============ Stock ============

    override suspend fun getStock(
        locationId: String?,
        variantId: String?,
    ): DataResult<List<StockItem>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(STOCK_VIEW).select {
                filter {
                    locationId?.let { eq("location_id", it) }
                    variantId?.let { eq("variant_id", it) }
                }
                order("producto", Order.ASCENDING)
            }.decodeList<StockActualDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getQuantity(variantId: String, locationId: String): DataResult<Int> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(STOCK).select {
                    filter {
                        eq("variant_id", variantId)
                        eq("location_id", locationId)
                    }
                }.decodeList<StockDto>()
            }.map { rows -> rows.firstOrNull()?.cantidad ?: 0 }
        }

    override suspend fun setStock(
        variantId: String,
        locationId: String,
        cantidad: Int,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(STOCK).upsert(
                StockUpsertDto(variantId = variantId, locationId = locationId, cantidad = cantidad)
            ) { onConflict = "variant_id,location_id" }
            Unit
        }
    }

    // ============ Transferencias ============

    override suspend fun getTransfers(
        openOnly: Boolean,
        locationId: String?,
    ): DataResult<List<StockTransfer>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TRANSFERS).select(TRANSFER_COLUMNS) {
                filter {
                    if (openOnly) isIn("estado", listOf("pendiente", "en_transito"))
                    // Involucra la tienda si sale de ella o llega a ella
                    locationId?.let { id ->
                        or {
                            eq("from_location_id", id)
                            eq("to_location_id", id)
                        }
                    }
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<StockTransferDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTransfer(id: String): DataResult<StockTransfer> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TRANSFERS).select(TRANSFER_COLUMNS) {
                    filter { eq("id", id) }
                }.decodeSingle<StockTransferDto>()
            }.map { it.toDomain() }
        }

    override suspend fun createTransfer(draft: TransferDraft): DataResult<StockTransfer> =
        withContext(io) {
            val userId = auth.currentUserOrNull()?.id
                ?: return@withContext DataResult.Error(DataError.Unauthorized)
            if (draft.items.isEmpty()) {
                return@withContext DataResult.Error(
                    DataError.Business("Una transferencia requiere al menos un artículo")
                )
            }

            // 1) Cabecera en 'pendiente' (RLS exige created_by = auth.uid())
            val header = safeSupabaseCall {
                postgrest.from(TRANSFERS).insert(
                    NewStockTransferDto(
                        fromLocationId = draft.fromLocationId,
                        toLocationId = draft.toLocationId,
                        notas = draft.notas,
                        createdBy = userId,
                    )
                ) { select() }.decodeSingle<StockTransferDto>()
            }
            val transfer = when (header) {
                is DataResult.Error -> return@withContext header
                is DataResult.Success -> header.data
            }

            // 2) Artículos. Si falla, la transferencia queda 'pendiente' y
            //    editable — no se pierde nada ni se movió stock.
            val itemsResult = safeSupabaseCall {
                postgrest.from(TRANSFER_ITEMS).insert(
                    draft.items.map { (variantId, cantidad) ->
                        NewStockTransferItemDto(
                            transferId = transfer.id,
                            variantId = variantId,
                            cantidad = cantidad,
                        )
                    }
                )
            }
            when (itemsResult) {
                is DataResult.Error -> itemsResult
                is DataResult.Success -> getTransfer(transfer.id)
            }
        }

    override suspend fun upsertTransferItem(
        transferId: String,
        variantId: String,
        cantidad: Int,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TRANSFER_ITEMS).upsert(
                NewStockTransferItemDto(transferId, variantId, cantidad)
            ) { onConflict = "transfer_id,variant_id" }
            Unit
        }
    }

    override suspend fun removeTransferItem(
        transferId: String,
        variantId: String,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TRANSFER_ITEMS).delete {
                filter {
                    eq("transfer_id", transferId)
                    eq("variant_id", variantId)
                }
            }
            Unit
        }
    }

    override suspend fun sendTransfer(id: String): DataResult<StockTransfer> =
        changeStatus(id, TransferStatus.EN_TRANSITO)

    override suspend fun receiveTransfer(id: String): DataResult<StockTransfer> =
        changeStatus(id, TransferStatus.RECIBIDA)

    override suspend fun cancelTransfer(id: String): DataResult<StockTransfer> =
        changeStatus(id, TransferStatus.CANCELADA)

    /**
     * Transición de estado: los triggers del servidor validan la máquina
     * de estados y mueven el stock. Errores llegan como DataError.Business.
     */
    private suspend fun changeStatus(
        id: String,
        status: TransferStatus,
    ): DataResult<StockTransfer> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TRANSFERS).update({ set("estado", status) }) {
                select(TRANSFER_COLUMNS)
                filter { eq("id", id) }
            }.decodeSingle<StockTransferDto>()
        }.map { it.toDomain() }
    }
}
