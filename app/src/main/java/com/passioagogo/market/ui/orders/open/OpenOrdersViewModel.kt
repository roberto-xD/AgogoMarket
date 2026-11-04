package com.passioagogo.market.ui.orders.open

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.customers.Customer
import com.passioagogo.market.data.customers.CustomerRepository
import com.passioagogo.market.domain.sales.Order
import com.passioagogo.market.domain.sales.SalesRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpenOrdersUiState(
    val pedidos: List<Order> = emptyList(),
    val customers: List<Customer> = emptyList(),
    /** Orden de la lista; los antiguos primero ayudan a limpiar. */
    val masNuevosPrimero: Boolean = true,
    val isLoading: Boolean = false,
    val porDescartar: Order? = null,
    val errorMessage: String? = null,
) {
    fun nombreCliente(customerId: String?): String? =
        customerId?.let { id -> customers.firstOrNull { it.id == id }?.nombre }
}

@HiltViewModel
class OpenOrdersViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OpenOrdersUiState())
    val uiState: StateFlow<OpenOrdersUiState> = _uiState.asStateFlow()

    fun onCambiarOrden() {
        _uiState.update { it.copy(masNuevosPrimero = !it.masNuevosPrimero) }
        refresh()
    }

    fun onPedirDescartar(pedido: Order) =
        _uiState.update { it.copy(porDescartar = pedido) }

    fun onCancelarDescartar() = _uiState.update { it.copy(porDescartar = null) }
    fun onCerrarError() = _uiState.update { it.copy(errorMessage = null) }

    fun onDescartar() {
        val pedido = _uiState.value.porDescartar ?: return
        _uiState.update { it.copy(porDescartar = null) }
        viewModelScope.launch {
            when (val r = salesRepository.descartarPedido(pedido.id)) {
                is DataResult.Success -> refresh()
                is DataResult.Error -> _uiState.update {
                    it.copy(errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val pedidos = salesRepository.getPedidosAbiertos(
                masNuevosPrimero = _uiState.value.masNuevosPrimero,
            )
            // Los clientes se cargan una vez: solo sirven para poner nombre
            // a los pedidos que ya lo tienen asignado.
            val clientes = if (_uiState.value.customers.isEmpty()) {
                customerRepository.getCustomers(includeInactive = true)
            } else null

            _uiState.update { estado ->
                estado.copy(
                    isLoading = false,
                    pedidos = (pedidos as? DataResult.Success)?.data ?: estado.pedidos,
                    customers = (clientes as? DataResult.Success)?.data
                        ?: estado.customers,
                    errorMessage = (pedidos as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }
}
