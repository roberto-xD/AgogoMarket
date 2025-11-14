package com.passioagogo.market.presentation.viewModel.products

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.AtributoProducto
import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.compras.GestionarStockUseCase
import com.passioagogo.market.domain.usecase.compras.MovimientoStockParams
import com.passioagogo.market.domain.usecase.compras.RegistrarCompraParams
import com.passioagogo.market.domain.usecase.compras.RegistrarCompraUseCase
import com.passioagogo.market.domain.usecase.compras.TipoMovimiento
import com.passioagogo.market.domain.usecase.producto.ActualizarProductoDetalladoUseCase
import com.passioagogo.market.domain.usecase.producto.CrearProductoUseCase
import com.passioagogo.market.domain.usecase.producto.EliminarProductoUseCase
import com.passioagogo.market.domain.usecase.producto.GuardarImagenParams
import com.passioagogo.market.domain.usecase.producto.GuardarImagenProductoUseCase
import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams
import com.passioagogo.market.domain.usecase.producto.ObtenerProductoDetalladoUseCase
import com.passioagogo.market.domain.usecase.producto.ValidarCodigoBarrasUseCase
import com.passioagogo.market.domain.usecase.producto.ValidarSkuParams
import com.passioagogo.market.domain.usecase.producto.ValidarSkuUseCase
import com.passioagogo.market.domain.usecase.ventas.RegistrarVentaParams
import com.passioagogo.market.domain.usecase.ventas.RegistrarVentaUseCase
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
    private val actualizarProductoDetalladoUseCase: ActualizarProductoDetalladoUseCase,
    private val crearProductoUseCase: CrearProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val gestionarStockUseCase: GestionarStockUseCase,
    private val registrarVentaUseCase: RegistrarVentaUseCase,
    private val registrarCompraUseCase: RegistrarCompraUseCase,
    private val guardarImagenProductoUseCase: GuardarImagenProductoUseCase,
    private val validarSkuUseCase: ValidarSkuUseCase,
    private val validarCodigoBarrasUseCase: ValidarCodigoBarrasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleProductoUiState())
    val uiState: StateFlow<DetalleProductoUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun cargarProducto(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            obtenerProductoDetalladoUseCase(id).onSuccess { productoDetallado ->
                _uiState.update {
                    it.copy(
                        productoDetallado = actualizarData(productoDetallado),
                        isLoading = false
                    )
                }
            }.onError { error ->
                Log.i(TAG_PG,"error al cargar: $error")
                _uiState.update {
                    it.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun actualizarProductoDetallado(datosProducto: GuardarProductoParams) {
        viewModelScope.launch {
            actualizarProductoDetalladoUseCase.invoke(datosProducto).onSuccess {
                Log.i(TAG_PG, "Producto actualizado exitosamente")
                _uiState.update {
                    it.copy(
                        mensajeExito = "Producto actualizado exitosamente"
                    )
                }
            }.onError { error ->
                Log.i(TAG_PG, "error al actualizar: $error")
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun crearProductoDetallado(datosProducto: GuardarProductoParams) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            crearProductoUseCase(datosProducto).onSuccess { nuevoId ->
                Log.i(TAG_PG,"Producto creado exitosamente: $nuevoId")

                _uiState.update {
                    it.copy(
                        productoDetallado = actualizarData(id = nuevoId),
                        isLoading = false,
                        mensajeExito = "Producto creado exitosamente"
                    )
                }
                // Los datos se actualizarán automáticamente por el Flow
            }.onError { error ->
                Log.i(TAG_PG,"error al crear: $error")
                _uiState.update {
                    it.copy(
                        errorMessage = when (error) {
                            is DomainException.SkuDuplicado -> "El SKU ya existe"
                            is DomainException.CodigoBarrasDuplicado -> "El código de barras ya existe"
                            is DomainException.PrecioInvalido -> "Precio inválido"
                            else -> error.message ?: "Error desconocido"
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun eliminarProducto(id: Long) {
        viewModelScope.launch {
            eliminarProductoUseCase(id).onSuccess {
                _uiState.update {
                    it.copy(mensajeExito = "Producto eliminado exitosamente")
                }
            }.onError { error ->
                Log.i(TAG_PG,"error al eliminar: $error")
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Error al eliminar producto")
                }
            }
        }
    }

    fun actualizarStock(nuevaCantidad: Int, tipoMovimiento: TipoMovimiento, motivo: String?) {
        val productoId = _uiState.value.productoDetallado.producto.id

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
                Log.i(TAG_PG,"error al actualizar: $error")
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
                Log.i(TAG_PG,"error al registrar venta: $error")
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
                Log.i(TAG_PG,"error al registrar compra: $error")
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
                Log.i(TAG_PG,"error al agregar imagen: $error")
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Error al agregar imagen")
                }
            }
        }
    }
    fun actualizarItem(
        productoDetallado: ProductoDetallado?
    ){
        _uiState.update {
            it.copy(
                productoDetallado = actualizarData(
                    productoDetallado = productoDetallado
                ),
                errorMessage = null,
                mensajeExito = null
            )
        }
    }

    private fun actualizarData(
        id: Long ?= null,
        nombre: String ?= null,
        descripcion: String?= null,
        imagenPrincipal: String?= null,
        codigoBarras: String?= null,
        skuInterno: String ?= null,
        precioCompra: Double ?= null,
        precioVenta: Double ?= null,
        cantidadActual: Int ?= null,
        idFamilia: Long ?= null,
        idCategoria: Long ?= null,
        idSubCategoria: Long ?= null,
        proveedores: List<ProveedorConPrecio> ?= null,
        atributos: List<AtributoProducto> ?= null,
        imagenes: List<ImagenProducto> ?= null,
    ) : ProductoDetallado {
        val producto = _uiState.value.productoDetallado.producto.let {
            it.copy(
                id = id ?: it.id,
                nombre = nombre ?: it.nombre,
                descripcion = descripcion ?: it.descripcion,
                imagenPrincipal = imagenPrincipal ?: it.imagenPrincipal,
                codigoBarras = codigoBarras ?: it.codigoBarras,
                skuInterno = skuInterno ?: it.skuInterno,
                precioCompra = precioCompra ?: it.precioCompra,
                precioVenta = precioVenta ?: it.precioVenta,
                cantidadActual = cantidadActual ?: it.cantidadActual,
            )
        }
        return _uiState.value.productoDetallado.let{
            it.copy(
                producto = producto,
                idFamilia = idFamilia ?: it.idFamilia,
                idCategoria = idCategoria ?: it.idCategoria,
                idSubCategoria = idSubCategoria ?: it.idSubCategoria,
                proveedores = proveedores ?: it.proveedores,
                atributos = atributos ?: it.atributos,
                imagenes = imagenes ?: it.imagenes
            )
        }
    }

    private fun actualizarData(productoDetallado: ProductoDetallado?) : ProductoDetallado{
        return actualizarData(
            id              = productoDetallado?.producto?.id,
            nombre          = productoDetallado?.producto?.nombre,
            descripcion     = productoDetallado?.producto?.descripcion,
            imagenPrincipal = productoDetallado?.producto?.imagenPrincipal,
            codigoBarras    = productoDetallado?.producto?.codigoBarras,
            skuInterno      = productoDetallado?.producto?.skuInterno,
            precioCompra    = productoDetallado?.producto?.precioCompra,
            precioVenta     = productoDetallado?.producto?.precioVenta,
            cantidadActual  = productoDetallado?.producto?.cantidadActual,
            idFamilia       = productoDetallado?.idFamilia,
            idCategoria     = productoDetallado?.idCategoria,
            idSubCategoria  = productoDetallado?.idSubCategoria,
            proveedores     = productoDetallado?.proveedores,
            atributos       = productoDetallado?.atributos,
            imagenes        = productoDetallado?.imagenes,
        )
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                productoDetallado = ProductoDetallado(),
                errorMessage = null,
                mensajeExito = null
            )
        }
    }
}