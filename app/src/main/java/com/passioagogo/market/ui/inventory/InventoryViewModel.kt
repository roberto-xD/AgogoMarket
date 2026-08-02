package com.passioagogo.market.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.preferences.UserPreferences
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.inventory.StockItem
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isAdmin: Boolean = false,
    /** Solo poblado para admin. */
    val locations: List<Location> = emptyList(),
    /** null = todas las ubicaciones (solo admin). */
    val selectedLocationId: String? = null,
    val query: String = "",
    val items: List<StockItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val filteredItems: List<StockItem>
        get() {
            if (query.isBlank()) return items
            val q = query.trim().lowercase()
            return items.filter {
                it.producto.lowercase().contains(q) ||
                    it.sku.lowercase().contains(q) ||
                    it.categoria.lowercase().contains(q)
            }
        }

    val valorTotal: Double get() = filteredItems.sumOf { it.valorInventario }
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
    private val userPreferences: UserPreferences,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        val session = authRepository.sessionState.value as? SessionState.Authenticated
        val isAdmin = session?.isAdmin == true
        _uiState.update { it.copy(isAdmin = isAdmin) }

        viewModelScope.launch {
            if (isAdmin) {
                // Ubicaciones para el selector + última usada
                when (val locs = locationRepository.getLocations()) {
                    is DataResult.Success ->
                        _uiState.update { it.copy(locations = locs.data) }
                    is DataResult.Error ->
                        _uiState.update { it.copy(errorMessage = locs.error.toMessage()) }
                }
                val saved = userPreferences.activeLocationId.first()
                _uiState.update { it.copy(selectedLocationId = saved) }
            } else {
                // Vendedor: fijo a su tienda (si no tiene, RLS devolverá vacío)
                _uiState.update { it.copy(selectedLocationId = session?.profile?.locationId) }
            }
            loadStock()
        }
    }

    fun onLocationSelected(locationId: String?) {
        _uiState.update { it.copy(selectedLocationId = locationId) }
        viewModelScope.launch {
            userPreferences.setActiveLocation(locationId)
            loadStock()
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun refresh() = viewModelScope.launch { loadStock() }

    private suspend fun loadStock() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = inventoryRepository.getStock(
            locationId = _uiState.value.selectedLocationId,
        )
        _uiState.update { state ->
            when (result) {
                is DataResult.Success ->
                    state.copy(isLoading = false, items = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}
