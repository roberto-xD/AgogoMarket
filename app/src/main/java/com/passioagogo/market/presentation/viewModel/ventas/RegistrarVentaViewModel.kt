package com.passioagogo.market.presentation.viewModel.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.model.venta.ProductoVenta
import com.passioagogo.market.domain.model.venta.Venta
import com.passioagogo.market.domain.usecase.ventas.GuardarVentaUseCase
import com.passioagogo.market.domain.usecase.ventas.ValidarStockUseCase
import com.passioagogo.market.presentation.view.components.ClienteModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrarVentaViewModel @Inject constructor(
    private val guardarVentaUseCase: GuardarVentaUseCase,
    private val validarStockUseCase: ValidarStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarVentaUiState())
    val uiState: StateFlow<RegistrarVentaUiState> = _uiState.asStateFlow()

    fun onClienteChange(cliente: ClienteModel) {
        _uiState.update { it.copy(clienteModel = cliente) }
    }

    fun onFechaLiquidacionChange(fecha: Long?) {
        _uiState.update { it.copy(fechaLiquidacion = fecha) }
    }

    fun onMetodoPagoChange(metodo: String) {
        _uiState.update { it.copy(metodoPago = metodo) }
    }

    fun onInstruccionesChange(instrucciones: String) {
        _uiState.update { it.copy(instruccionesEntrega = instrucciones) }
    }

    fun agregarProducto(producto: ProductoVenta) {
        val productosActuales = _uiState.value.productos.toMutableList()
        val indiceExistente = productosActuales.indexOfFirst { it.productoId == producto.productoId }

        if (indiceExistente != -1) {
            productosActuales[indiceExistente] = producto
        } else {
            productosActuales.add(producto)
        }

        _uiState.update {
            it.copy(
                productos = productosActuales,
                total = calcularTotal(productosActuales)
            )
        }
        validarStockEnTiempoReal()
    }

    fun eliminarProducto(productoId: Long) {
        val productosActualizados = _uiState.value.productos.filter { it.productoId != productoId }
        _uiState.update {
            it.copy(
                productos = productosActualizados,
                total = calcularTotal(productosActualizados)
            )
        }
    }

    fun actualizarCantidadProducto(productoId: Long, cantidad: Int) {
        if (cantidad <= 0) {
            eliminarProducto(productoId)
            return
        }

        val productosActualizados = _uiState.value.productos.map { producto ->
            if (producto.productoId == productoId) {
                producto.copy(cantidad = cantidad)
            } else {
                producto
            }
        }

        _uiState.update {
            it.copy(
                productos = productosActualizados,
                total = calcularTotal(productosActualizados)
            )
        }
        validarStockEnTiempoReal()
    }

    private fun validarStockEnTiempoReal() {
        viewModelScope.launch {
            val resultado = validarStockUseCase(_uiState.value.productos)
            _uiState.update {
                it.copy(errorStock = resultado.exceptionOrNull()?.message)
            }
        }
    }

    private fun calcularTotal(productos: List<ProductoVenta>): Double {
        return productos.sumOf { it.subtotal }
    }

    fun guardarVenta() {
        if (!validarFormulario()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val venta = Venta(
                fechaLiquidacion = _uiState.value.fechaLiquidacion,
                total = _uiState.value.total,
                metodoPago = _uiState.value.metodoPago,
                instruccionesEntrega = _uiState.value.instruccionesEntrega,
                productos = _uiState.value.productos
            )

            guardarVentaUseCase(venta).fold(
                onSuccess = { ventaId ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ventaGuardada = true,
                            ventaId = ventaId
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al guardar la venta"
                        )
                    }
                }
            )
        }
    }

    private fun validarFormulario(): Boolean {
        when {
            _uiState.value.productos.isEmpty() -> {
                _uiState.update { it.copy(error = "Debe agregar al menos un producto") }
                return false
            }
            _uiState.value.errorStock != null -> {
                _uiState.update { it.copy(error = _uiState.value.errorStock) }
                return false
            }
        }
        return true
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetearFormulario() {
        _uiState.value = RegistrarVentaUiState()
    }
}

data class RegistrarVentaUiState(
    val clienteModel: ClienteModel = ClienteModel(),
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaLiquidacion: Long? = null,
    val total: Double = 0.0,
    val metodoPago: String = "",
    val instruccionesEntrega: String = "",
    val productos: List<ProductoVenta> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorStock: String? = null,
    val ventaGuardada: Boolean = false,
    val ventaId: Long? = null
)