package com.passioagogo.market.presentation.viewModel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasPorFamiliaUseCase
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasUseCase
import com.passioagogo.market.domain.usecase.producto.BuscarProductosParams
import com.passioagogo.market.domain.usecase.producto.BuscarProductosUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosStockBajoUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProveedoresUseCase
import com.passioagogo.market.presentation.uiState.ProductosUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obtenerProductosUseCase: ObtenerProductosUseCase,
    private val obtenerProductosStockBajoUseCase: ObtenerProductosStockBajoUseCase,
    private val buscarProductosUseCase: BuscarProductosUseCase,
    private val obtenerCategoriasUseCase: ObtenerCategoriasUseCase,
    private val obtenerProveedoresUseCase: ObtenerProveedoresUseCase,
    private val obtenerCategoriasPorFamiliaUseCase: ObtenerCategoriasPorFamiliaUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // Cargar productos
            obtenerProductosUseCase().onSuccess { productosFlow ->
                productosFlow.collect { productos ->
                    _uiState.update { it.copy(productos = productos, isLoading = false) }
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
            }

            // Cargar productos con stock bajo
            obtenerProductosStockBajoUseCase().onSuccess { stockBajoFlow ->
                stockBajoFlow.collect { productosStockBajo ->
                    _uiState.update { it.copy(productosStockBajo = productosStockBajo) }
                }
            }

            // Cargar categorías
            obtenerCategoriasUseCase().onSuccess { categoriasFlow ->
                categoriasFlow.collect { categorias ->
                    _uiState.update { it.copy(categorias = categorias) }
                }
            }

            // Cargar proveedores
            obtenerProveedoresUseCase().onSuccess { proveedoresFlow ->
                proveedoresFlow.collect { proveedores ->
                    _uiState.update { it.copy(proveedores = proveedores) }
                }
            }
        }
    }

    fun buscarProductos(query: String) {
        if (query.isBlank()) {
            cargarDatosIniciales()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val params = BuscarProductosParams(query = query)
            buscarProductosUseCase(params).onSuccess { resultadosFlow ->
                resultadosFlow.collect { productos ->
                    _uiState.update {
                        it.copy(
                            productos = productos,
                            isLoading = false
                        )
                    }
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


    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                mensajeExito = null
            )
        }
    }

    fun filtrarPorCategoria(categoriaId: Long?) {
        _uiState.update { it.copy(categoriaSeleccionada = categoriaId) }
        // Implementar filtrado local o recargar datos
    }

    fun obtenerCategoriasPorFamilia(familia: String) {
        viewModelScope.launch {
            obtenerCategoriasPorFamiliaUseCase.invoke(familia).onSuccess { categoriasFlow ->
                categoriasFlow.collect { categorias ->
                    _uiState.update { it.copy(categorias = categorias) }
                }
            }
        }
    }

}