package com.passioagogo.market.ui.orders.open

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.customers.Customer
import com.passioagogo.market.data.customers.CustomerRepository
import com.passioagogo.market.data.customers.ShippingAddress
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.sales.Order
import com.passioagogo.market.domain.sales.OrderItem
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

data class EditOpenOrderUiState(
    val pedido: Order? = null,
    val items: List<OrderItem> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val addresses: List<ShippingAddress> = emptyList(),
    val catalog: List<VariantInfo> = emptyList(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    /** Existencia en la ubicación del pedido, para avisar antes de confirmar. */
    val stock: Map<String, Int> = emptyMap(),
    val query: String = "",
    val notas: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val confirmado: Boolean = false,
    val errorMessage: String? = null,
) {
    val searchResults: List<VariantInfo>
        get() {
            if (query.isBlank()) return emptyList()
            val q = query.trim().lowercase()
            return catalog.filter {
                it.producto.lowercase().contains(q) ||
                    it.sku.lowercase().contains(q) ||
                    it.titulo?.lowercase()?.contains(q) == true
            }.take(15)
        }

    fun disponible(variantId: String): Int = stock[variantId] ?: 0

    /** Líneas que superan la existencia: el servidor las rechazará. */
    val sinExistencia: List<OrderItem>
        get() = items.filter { it.cantidad > disponible(it.variantId) }

    val puedeConfirmar: Boolean
        get() = !isSaving && items.isNotEmpty() && sinExistencia.isEmpty() &&
            pedido?.customerId != null
}

@HiltViewModel
class EditOpenOrderViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val customerRepository: CustomerRepository,
    private val catalogRepository: CatalogRepository,
    private val inventoryRepository: InventoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(EditOpenOrderUiState())
    val uiState: StateFlow<EditOpenOrderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val index = catalogRepository.variantIndex()
            val clientes = customerRepository.getCustomers()
            _uiState.update {
                it.copy(
                    catalog = index.values.sortedBy { v -> v.producto },
                    variantIndex = index,
                    customers = (clientes as? DataResult.Success)?.data ?: emptyList(),
                )
            }
            cargar()
        }
    }

    fun onQuery(v: String) = _uiState.update { it.copy(query = v) }
    fun onNotas(v: String) = _uiState.update { it.copy(notas = v) }
    fun onCerrarError() = _uiState.update { it.copy(errorMessage = null) }

    fun onAgregar(variantId: String) {
        _uiState.update { it.copy(isSaving = true, query = "") }
        viewModelScope.launch {
            // Una unidad por toque; la cantidad se ajusta después. El
            // servidor recotiza con la promoción del día.
            val r = salesRepository.agregarLinea(orderId, variantId, 1)
            if (r is DataResult.Error) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            } else {
                cargar()
            }
        }
    }

    fun onQuitar(item: OrderItem) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val r = salesRepository.quitarLinea(orderId, item.id)
            if (r is DataResult.Error) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            } else {
                cargar()
            }
        }
    }

    fun onCliente(customerId: String) {
        viewModelScope.launch {
            salesRepository.actualizarDatosPedido(orderId, customerId = customerId)
            cargarDirecciones(customerId)
            cargar()
        }
    }

    fun onDireccion(addressId: String) {
        viewModelScope.launch {
            salesRepository.actualizarDatosPedido(orderId, shippingAddressId = addressId)
            cargar()
        }
    }

    fun onGuardarNotas() {
        viewModelScope.launch {
            salesRepository.actualizarDatosPedido(
                orderId,
                notas = _uiState.value.notas.trim(),
            )
        }
    }

    /**
     * Confirma el pedido: aquí se descuenta el stock.
     *
     * El pago se registra después, como en el resto de envíos: así caben
     * los pagos parciales y el pedido queda con su saldo a la vista.
     */
    fun onConfirmar() {
        if (!_uiState.value.puedeConfirmar) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = salesRepository.confirmarPedido(orderId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isSaving = false, confirmado = true) }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    private suspend fun cargarDirecciones(customerId: String) {
        val dirs = customerRepository.getAddresses(customerId)
        _uiState.update {
            it.copy(addresses = (dirs as? DataResult.Success)?.data ?: emptyList())
        }
    }

    private suspend fun cargar() {
        _uiState.update { it.copy(isLoading = true) }
        val pedido = salesRepository.getOrder(orderId)
        val datos = (pedido as? DataResult.Success)?.data

        // La existencia se relee en cada cambio: un pedido abierto no
        // reserva nada, así que lo que había al crearlo puede no estar.
        val stock = datos?.locationId?.let { inventoryRepository.getStock(locationId = it) }

        _uiState.update { estado ->
            estado.copy(
                isLoading = false,
                pedido = datos,
                items = datos?.items.orEmpty(),
                notas = estado.notas.ifBlank { datos?.notas.orEmpty() },
                stock = (stock as? DataResult.Success)?.data
                    ?.associate { it.variantId to it.cantidad } ?: estado.stock,
                errorMessage = (pedido as? DataResult.Error)?.error?.toMessage(),
            )
        }
        datos?.customerId?.let { if (_uiState.value.addresses.isEmpty()) cargarDirecciones(it) }
    }
}
