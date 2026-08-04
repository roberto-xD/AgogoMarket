package com.passioagogo.market.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.common.OrderStatus
import com.passioagogo.market.domain.common.PaymentMethod
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.sales.Order
import com.passioagogo.market.domain.sales.SalesRepository
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

internal val OrderStatus.etiqueta: String
    get() = when (this) {
        OrderStatus.PENDIENTE -> "Pendiente"
        OrderStatus.CONFIRMADO -> "Confirmado"
        OrderStatus.EN_TRANSITO -> "En tránsito"
        OrderStatus.ENTREGADO -> "Entregado"
        OrderStatus.CANCELADO -> "Cancelado"
    }

internal val OrderStatus.esCancelable: Boolean
    get() = this == OrderStatus.PENDIENTE ||
        this == OrderStatus.CONFIRMADO ||
        this == OrderStatus.EN_TRANSITO

// ============ Historial ============

data class OrdersUiState(
    val isAdmin: Boolean = false,
    val locations: List<Location> = emptyList(),
    /** null = todas (solo admin). */
    val locationId: String? = null,
    val openOnly: Boolean = true,
    val orders: List<Order> = emptyList(),
    val locationNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val visibleOrders: List<Order>
        get() = if (!openOnly) orders
        else orders.filter { it.estado != OrderStatus.CANCELADO && it.estado != OrderStatus.ENTREGADO }
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val locationRepository: LocationRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        val session = authRepository.sessionState.value as? SessionState.Authenticated
        val isAdmin = session?.isAdmin == true
        _uiState.update {
            it.copy(
                isAdmin = isAdmin,
                locationId = if (isAdmin) null else session?.profile?.locationId,
            )
        }
        viewModelScope.launch {
            val locs = locationRepository.getLocations(includeInactive = true)
            if (locs is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(
                        locations = locs.data.filter { it.activo },
                        locationNames = locs.data.associate { it.id to it.nombre },
                    )
                }
            }
            load()
        }
    }

    fun onLocationSelected(id: String?) {
        _uiState.update { it.copy(locationId = id) }
        refresh()
    }

    fun onToggleOpenOnly(openOnly: Boolean) = _uiState.update { it.copy(openOnly = openOnly) }

    fun refresh() = viewModelScope.launch { load() }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = salesRepository.getRecentOrders(
            locationId = _uiState.value.locationId,
            limit = 50,
        )
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, orders = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

// ============ Detalle ============

data class OrderDetailUiState(
    val order: Order? = null,
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val locationNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isCancelling: Boolean = false,
    val showCancelConfirm: Boolean = false,
    val showShipDialog: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val errorMessage: String? = null,
) {
    val saldo: Double
        get() = order?.let { o -> o.total - o.payments.sumOf { it.monto } } ?: 0.0
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

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

    fun onAskCancel() = _uiState.update { it.copy(showCancelConfirm = true) }
    fun onOpenShipDialog() = _uiState.update { it.copy(showShipDialog = true) }
    fun onDismissShipDialog() = _uiState.update { it.copy(showShipDialog = false) }
    fun onOpenPaymentDialog() = _uiState.update { it.copy(showPaymentDialog = true) }
    fun onDismissPaymentDialog() = _uiState.update { it.copy(showPaymentDialog = false) }

    fun onShip(paqueteria: String, guia: String) {
        _uiState.update {
            it.copy(isCancelling = true, showShipDialog = false, errorMessage = null)
        }
        viewModelScope.launch {
            when (val r = salesRepository.shipOrder(orderId, paqueteria, guia)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isCancelling = false, order = r.data) }
                is DataResult.Error -> _uiState.update {
                    it.copy(isCancelling = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onDeliver() {
        _uiState.update { it.copy(isCancelling = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = salesRepository.deliverOrder(orderId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isCancelling = false, order = r.data) }
                is DataResult.Error -> _uiState.update {
                    it.copy(isCancelling = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onAddPayment(monto: Double, metodo: PaymentMethod, referencia: String?) {
        _uiState.update {
            it.copy(isCancelling = true, showPaymentDialog = false, errorMessage = null)
        }
        viewModelScope.launch {
            when (val r = salesRepository.addPayment(orderId, monto, metodo, referencia)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isCancelling = false, order = r.data) }
                is DataResult.Error -> _uiState.update {
                    it.copy(isCancelling = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }
    fun onDismissCancel() = _uiState.update { it.copy(showCancelConfirm = false) }

    fun onConfirmCancel() {
        if (_uiState.value.isCancelling) return
        _uiState.update {
            it.copy(isCancelling = true, showCancelConfirm = false, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = salesRepository.cancelOrder(orderId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isCancelling = false, order = result.data) }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(isCancelling = false, errorMessage = result.error.toMessage())
                    }
            }
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = salesRepository.getOrder(orderId)
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, order = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}
