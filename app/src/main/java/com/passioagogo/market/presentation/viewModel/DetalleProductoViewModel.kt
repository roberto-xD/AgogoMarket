package com.passioagogo.market.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.ActualizarProductoUseCase
import com.passioagogo.market.domain.usecase.GestionarStockUseCase
import com.passioagogo.market.domain.usecase.GuardarImagenParams
import com.passioagogo.market.domain.usecase.GuardarImagenProductoUseCase
import com.passioagogo.market.domain.usecase.MovimientoStockParams
import com.passioagogo.market.domain.usecase.ObtenerProductoDetalladoUseCase
import com.passioagogo.market.domain.usecase.RegistrarCompraParams
import com.passioagogo.market.domain.usecase.RegistrarCompraUseCase
import com.passioagogo.market.domain.usecase.RegistrarVentaParams
import com.passioagogo.market.domain.usecase.RegistrarVentaUseCase
import com.passioagogo.market.domain.usecase.TipoMovimiento
import com.passioagogo.market.domain.usecase.ValidarCodigoBarrasUseCase
import com.passioagogo.market.domain.usecase.ValidarSkuParams
import com.passioagogo.market.domain.usecase.ValidarSkuUseCase
import com.passioagogo.market.presentation.uiState.DetalleProductoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleProductoViewModel @Inject constructor(
    private val obtenerProductoDetalladoUseCase: ObtenerProductoDetalladoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val gestionarStockUseCase: GestionarStockUseCase,
    private val registrarVentaUseCase: RegistrarVentaUseCase,
    private val registrarCompraUseCase: RegistrarCompraUseCase,
    private val guardarImagenProductoUseCase: GuardarImagenProductoUseCase,
    private val validarSkuUseCase: ValidarSkuUseCase,
    private val validarCodigoBarrasUseCase: ValidarCodigoBarrasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleProductoUiState())
    val uiState: StateFlow<DetalleProductoUiState> = _uiState.asStateFlow()

    fun cargarProducto(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            obtenerProductoDetalladoUseCase(id).onSuccess { productoDetallado ->
                _uiState.update {
                    it.copy(
                        productoDetallado = productoDetallado,
                        isLoading = false
                    )
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun actualizarStock(nuevaCantidad: Int, tipoMovimiento: TipoMovimiento, motivo: String?) {
        val productoId = _uiState.value.productoDetallado?.producto?.id ?: return

        viewModelScope.launch {
            val params = MovimientoStockParams(
                productoId = productoId,
                cantidad = nuevaCantidad,
                tipoMovimiento = tipoMovimiento,
                motivo = motivo
            )

            gestionarStockUseCase(params).onSuccess {
                // Recargar el producto para mostrar datos actualizados
                cargarProducto(productoId)
                _uiState.update {
                    it.copy(mensajeExito = "Stock actualizado exitosamente")
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = when (error) {
                            is DomainException.StockInsuficiente -> "Stock insuficiente"
                            is DomainException.CantidadInvalida -> "Cantidad inválida"
                            else -> error.message ?: "Error al actualizar stock"
                        }
                    )
                }
            }
        }
    }

    fun registrarVenta(cantidad: Int) {
        val productoId = _uiState.value.productoDetallado?.producto?.id ?: return

        viewModelScope.launch {
            val params = RegistrarVentaParams(productoId = productoId, cantidad = cantidad)

            registrarVentaUseCase(params).onSuccess {
                cargarProducto(productoId)
                _uiState.update {
                    it.copy(mensajeExito = "Venta registrada exitosamente")
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Error al registrar venta")
                }
            }
        }
    }

    fun registrarCompra(cantidad: Int, precioCompra: Double? = null) {
        val productoId = _uiState.value.productoDetallado?.producto?.id ?: return

        viewModelScope.launch {
            val params = RegistrarCompraParams(
                productoId = productoId,
                cantidad = cantidad,
                precioCompra = precioCompra
            )

            registrarCompraUseCase(params).onSuccess {
                cargarProducto(productoId)
                _uiState.update {
                    it.copy(mensajeExito = "Compra registrada exitosamente")
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Error al registrar compra")
                }
            }
        }
    }

    fun validarSku(sku: String) {
        val productoId = _uiState.value.productoDetallado?.producto?.id

        viewModelScope.launch {
            val params = ValidarSkuParams(sku = sku, productoIdExcluir = productoId)

            validarSkuUseCase(params).onSuccess { esValido ->
                _uiState.update {
                    it.copy(skuValido = esValido)
                }
            }
        }
    }

    fun validarCodigoBarras(codigo: String) {
        viewModelScope.launch {
            validarCodigoBarrasUseCase(codigo).onSuccess { esValido ->
                _uiState.update {
                    it.copy(codigoBarrasValido = esValido)
                }
            }
        }
    }

    fun agregarImagen(rutaImagen: String, esPrincipal: Boolean = false) {
        val productoId = _uiState.value.productoDetallado?.producto?.id ?: return

        viewModelScope.launch {
            val params = GuardarImagenParams(
                productoId = productoId,
                rutaImagen = rutaImagen,
                esPrincipal = esPrincipal
            )

            guardarImagenProductoUseCase(params).onSuccess { imagenId ->
                cargarProducto(productoId)
                _uiState.update {
                    it.copy(mensajeExito = "Imagen agregada exitosamente")
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Error al agregar imagen")
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                mensajeExito = null
            )
        }
    }
}