package com.passioagogo.market.ui.inventory.transfers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.inventory.StockTransfer
import com.passioagogo.market.domain.inventory.TransferDraft
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Info mínima de una variante para pintar líneas de transferencia. */
data class VariantInfo(
    val variantId: String,
    val producto: String,
    val sku: String,
    val atributos: String,
    val costo: Double = 0.0,
)

/** Aplana el catálogo cacheado a un índice variantId → info. */
internal suspend fun CatalogRepository.variantIndex(): Map<String, VariantInfo> {
    var products = observeProducts().first()
    if (products.isEmpty()) {
        refreshCatalog()
        products = observeProducts().first()
    }
    return products.flatMap { pw ->
        pw.variants.map { v ->
            VariantInfo(
                variantId = v.id,
                producto = pw.product.nombre,
                sku = v.sku,
                atributos = v.attributes.entries.joinToString(" · ") { (k, value) ->
                    "$k: ${value.toString().trim('"')}"
                },
                costo = v.costo,
            )
        }
    }.associateBy { it.variantId }
}

// ============ Lista ============

data class TransfersUiState(
    val openOnly: Boolean = true,
    val transfers: List<StockTransfer> = emptyList(),
    val locationNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransfersUiState())
    val uiState: StateFlow<TransfersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val locs = locationRepository.getLocations(includeInactive = true)
            if (locs is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(locationNames = locs.data.associate { it.id to it.nombre })
                }
            }
            load()
        }
    }

    fun onToggleOpenOnly(openOnly: Boolean) {
        _uiState.update { it.copy(openOnly = openOnly) }
        refresh()
    }

    fun refresh() = viewModelScope.launch { load() }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = inventoryRepository.getTransfers(openOnly = _uiState.value.openOnly)
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, transfers = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

// ============ Detalle ============

data class TransferDetailUiState(
    val transfer: StockTransfer? = null,
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val locationNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isActing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TransferDetailViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val transferId: String = checkNotNull(savedStateHandle["transferId"])

    private val _uiState = MutableStateFlow(TransferDetailUiState())
    val uiState: StateFlow<TransferDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val locs = locationRepository.getLocations(includeInactive = true)
            if (locs is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(locationNames = locs.data.associate { it.id to it.nombre })
                }
            }
            _uiState.update { it.copy(variantIndex = catalogRepository.variantIndex()) }
            load()
        }
    }

    fun refresh() = viewModelScope.launch { load() }

    fun send() = act { inventoryRepository.sendTransfer(transferId) }
    fun receive() = act { inventoryRepository.receiveTransfer(transferId) }
    fun cancel() = act { inventoryRepository.cancelTransfer(transferId) }

    private fun act(operation: suspend () -> DataResult<StockTransfer>) {
        if (_uiState.value.isActing) return
        _uiState.update { it.copy(isActing = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = operation()) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isActing = false, transfer = result.data) }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(isActing = false, errorMessage = result.error.toMessage())
                    }
            }
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = inventoryRepository.getTransfer(transferId)
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, transfer = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

// ============ Creación ============

data class CreateTransferUiState(
    val locations: List<Location> = emptyList(),
    val fromLocationId: String? = null,
    val toLocationId: String? = null,
    val notas: String = "",
    val query: String = "",
    val catalog: List<VariantInfo> = emptyList(),
    /** variantId → cantidad */
    val lines: Map<String, Int> = emptyMap(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    /** Id de la transferencia creada: dispara la navegación de regreso. */
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
        get() = !isSaving && fromLocationId != null && toLocationId != null &&
            fromLocationId != toLocationId && lines.isNotEmpty()
}

@HiltViewModel
class CreateTransferViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTransferUiState())
    val uiState: StateFlow<CreateTransferUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val locs = locationRepository.getLocations()
            val index = catalogRepository.variantIndex()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    locations = (locs as? DataResult.Success)?.data ?: emptyList(),
                    catalog = index.values.sortedBy { it.producto },
                    variantIndex = index,
                    errorMessage = (locs as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    fun onFromSelected(id: String) = _uiState.update { it.copy(fromLocationId = id) }
    fun onToSelected(id: String) = _uiState.update { it.copy(toLocationId = id) }
    fun onNotasChange(value: String) = _uiState.update { it.copy(notas = value) }
    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onAddVariant(variantId: String) = _uiState.update { state ->
        val current = state.lines[variantId] ?: 0
        state.copy(lines = state.lines + (variantId to current + 1), query = "")
    }

    fun onQuantityChange(variantId: String, cantidad: Int) = _uiState.update { state ->
        if (cantidad <= 0) state.copy(lines = state.lines - variantId)
        else state.copy(lines = state.lines + (variantId to cantidad))
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = inventoryRepository.createTransfer(
                TransferDraft(
                    fromLocationId = state.fromLocationId!!,
                    toLocationId = state.toLocationId!!,
                    notas = state.notas.ifBlank { null },
                    items = state.lines,
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
