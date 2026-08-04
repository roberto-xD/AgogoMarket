package com.passioagogo.market.ui.admin.purchases

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.common.PurchaseStatus
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.purchases.Purchase
import com.passioagogo.market.domain.purchases.PurchaseDraft
import com.passioagogo.market.domain.purchases.PurchaseLine
import com.passioagogo.market.domain.purchases.PurchaseRepository
import com.passioagogo.market.domain.purchases.Supplier
import com.passioagogo.market.domain.purchases.SupplierRepository
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.transfers.VariantInfo
import com.passioagogo.market.ui.inventory.transfers.variantIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal val PurchaseStatus.etiqueta: String
    get() = when (this) {
        PurchaseStatus.PENDIENTE -> "Pendiente"
        PurchaseStatus.RECIBIDA -> "Recibida"
        PurchaseStatus.CANCELADA -> "Cancelada"
    }

// ============ Lista ============

data class PurchasesUiState(
    val pendingOnly: Boolean = true,
    val purchases: List<Purchase> = emptyList(),
    val supplierNames: Map<String, String> = emptyMap(),
    val locationNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchasesUiState())
    val uiState: StateFlow<PurchasesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val suppliers = supplierRepository.getSuppliers(includeInactive = true)
            if (suppliers is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(supplierNames = suppliers.data.associate { it.id to it.nombre })
                }
            }
            val locs = locationRepository.getLocations(includeInactive = true)
            if (locs is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(locationNames = locs.data.associate { it.id to it.nombre })
                }
            }
            load()
        }
    }

    fun onTogglePendingOnly(pendingOnly: Boolean) {
        _uiState.update { it.copy(pendingOnly = pendingOnly) }
        refresh()
    }

    fun refresh() = viewModelScope.launch { load() }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = purchaseRepository.getPurchases(
            pendingOnly = _uiState.value.pendingOnly,
        )
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, purchases = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

// ============ Detalle ============

data class PurchaseDetailUiState(
    val purchase: Purchase? = null,
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val supplierName: String? = null,
    val locationName: String? = null,
    val isLoading: Boolean = true,
    val isActing: Boolean = false,
    val confirmAction: PurchaseAction? = null,
    val errorMessage: String? = null,
)

enum class PurchaseAction { RECIBIR, CANCELAR }

@HiltViewModel
class PurchaseDetailViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val purchaseId: String = checkNotNull(savedStateHandle["purchaseId"])

    private val _uiState = MutableStateFlow(PurchaseDetailUiState())
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(variantIndex = catalogRepository.variantIndex()) }
            load()
        }
    }

    fun onAsk(action: PurchaseAction) = _uiState.update { it.copy(confirmAction = action) }
    fun onDismissConfirm() = _uiState.update { it.copy(confirmAction = null) }

    fun onConfirm() {
        val action = _uiState.value.confirmAction ?: return
        _uiState.update { it.copy(isActing = true, confirmAction = null, errorMessage = null) }
        viewModelScope.launch {
            val result = when (action) {
                PurchaseAction.RECIBIR -> purchaseRepository.receivePurchase(purchaseId)
                PurchaseAction.CANCELAR -> purchaseRepository.cancelPurchase(purchaseId)
            }
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success ->
                        state.copy(isActing = false, purchase = result.data)
                    is DataResult.Error ->
                        state.copy(isActing = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = purchaseRepository.getPurchase(purchaseId)) {
            is DataResult.Success -> {
                val purchase = result.data
                var supplierName: String? = null
                val suppliers = supplierRepository.getSuppliers(includeInactive = true)
                if (suppliers is DataResult.Success) {
                    supplierName = suppliers.data
                        .firstOrNull { it.id == purchase.supplierId }?.nombre
                }
                var locationName: String? = null
                val locs = locationRepository.getLocations(includeInactive = true)
                if (locs is DataResult.Success) {
                    locationName = locs.data
                        .firstOrNull { it.id == purchase.locationId }?.nombre
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        purchase = purchase,
                        supplierName = supplierName,
                        locationName = locationName,
                    )
                }
            }
            is DataResult.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

// ============ Creación ============

data class PurchaseLineInput(val cantidad: Int, val costo: String)

data class CreatePurchaseUiState(
    val suppliers: List<Supplier> = emptyList(),
    val locations: List<Location> = emptyList(),
    val supplierId: String? = null,
    val locationId: String? = null,
    val notas: String = "",
    val query: String = "",
    val catalog: List<VariantInfo> = emptyList(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    /** variantId → línea editable */
    val lines: Map<String, PurchaseLineInput> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdId: String? = null,
) {
    val searchResults: List<VariantInfo>
        get() {
            if (query.isBlank()) return emptyList()
            val q = query.trim().lowercase()
            return catalog.filter {
                it.producto.lowercase().contains(q) || it.sku.lowercase().contains(q)
            }.take(15)
        }

    val canSave: Boolean
        get() = !isSaving && supplierId != null && locationId != null &&
            lines.isNotEmpty() &&
            lines.values.all { it.cantidad > 0 && it.costo.toDoubleOrNull() != null }
}

@HiltViewModel
class CreatePurchaseViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePurchaseUiState())
    val uiState: StateFlow<CreatePurchaseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val suppliers = supplierRepository.getSuppliers()
            val locs = locationRepository.getLocations()
            val index = catalogRepository.variantIndex()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    suppliers = (suppliers as? DataResult.Success)?.data ?: emptyList(),
                    locations = (locs as? DataResult.Success)?.data ?: emptyList(),
                    catalog = index.values.sortedBy { it.producto },
                    variantIndex = index,
                    errorMessage = (suppliers as? DataResult.Error)?.error?.toMessage()
                        ?: (locs as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    fun onSupplierSelected(id: String) = _uiState.update { it.copy(supplierId = id) }
    fun onLocationSelected(id: String) = _uiState.update { it.copy(locationId = id) }
    fun onNotasChange(value: String) = _uiState.update { it.copy(notas = value) }
    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onAddVariant(info: VariantInfo) = _uiState.update { state ->
        val current = state.lines[info.variantId]
        val line = current?.copy(cantidad = current.cantidad + 1)
            ?: PurchaseLineInput(cantidad = 1, costo = info.costo.toString())
        state.copy(lines = state.lines + (info.variantId to line), query = "")
    }

    fun onLineChange(variantId: String, cantidad: Int, costo: String) =
        _uiState.update { state ->
            if (cantidad <= 0) state.copy(lines = state.lines - variantId)
            else state.copy(
                lines = state.lines +
                    (variantId to PurchaseLineInput(cantidad = cantidad, costo = costo))
            )
        }

    fun onRemoveLine(variantId: String) =
        _uiState.update { it.copy(lines = it.lines - variantId) }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = purchaseRepository.createPurchase(
                PurchaseDraft(
                    supplierId = state.supplierId!!,
                    locationId = state.locationId!!,
                    notas = state.notas.ifBlank { null },
                    items = state.lines.mapValues { (_, line) ->
                        PurchaseLine(
                            cantidad = line.cantidad,
                            costoUnitario = line.costo.toDouble(),
                        )
                    },
                )
            )
            _uiState.update {
                when (result) {
                    is DataResult.Success ->
                        it.copy(isSaving = false, createdId = result.data.id)
                    is DataResult.Error ->
                        it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}
