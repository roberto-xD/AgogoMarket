package com.passioagogo.market.presentation.viewModel.products

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaParams
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaUseCase
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasPorFamiliaIdUseCase
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasUseCase
import com.passioagogo.market.domain.usecase.familias.ObtenerFamiliasUseCase
import com.passioagogo.market.domain.usecase.producto.BuscarProductosParams
import com.passioagogo.market.domain.usecase.producto.BuscarProductosUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosStockBajoUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProveedoresUseCase
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaParams
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaUseCase
import com.passioagogo.market.domain.usecase.subcategorias.ObtenerSubcategoriaPorCategoriaUseCase
import com.passioagogo.market.presentation.uiState.CatalogoUiState
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
    private val obtenerFamiliasUseCase: ObtenerFamiliasUseCase,
    private val obtenerCategoriasUseCase: ObtenerCategoriasUseCase,
    private val obtenerProveedoresUseCase: ObtenerProveedoresUseCase,
    private val obtenerCategoriasPorFamiliaIdUseCase: ObtenerCategoriasPorFamiliaIdUseCase,
    private val obtenerSubcategoriaPorCategoriaUseCase: ObtenerSubcategoriaPorCategoriaUseCase,
    private val crearCategoriaUseCase: CrearCategoriaConFamiliaUseCase,
    private val crearSubcategoriaUseCase: CrearSubcategoriaConCategoriaUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        Log.i(TAG_PG,"DashboardViewModel init")
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            //Cargar familias
            obtenerFamiliasUseCase().onSuccess { familiasFlow ->
                familiasFlow.collect { familias ->
                    Log.i(TAG_PG,"familias: $familias")
                    _uiState.update { it.copy(familias = familias) }
                }
            }
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

    fun obtenerCategoriasPorFamiliaId(familia: Long) {
        viewModelScope.launch {
            obtenerCategoriasPorFamiliaIdUseCase.invoke(familia).onSuccess { categoriasFlow ->
                categoriasFlow.collect { categorias ->
                    _uiState.update { it.copy(categorias = categorias) }
                }
            }
        }
    }

    fun obtenerSubcategoriasPorCategoria(categoriaId: Long){
        viewModelScope.launch {
            obtenerSubcategoriaPorCategoriaUseCase.invoke(categoriaId)
                .onSuccess { subcategoriasFlow ->
                    subcategoriasFlow.collect { subcategorias ->
                        _uiState.update {
                            it.copy(
                                subcategorias = subcategorias
                            )
                        }
                    }
                }
        }
    }

    fun crearCategoriaConFamilia(
        nombre: String,
        descripcion: String,
        familiaId: Long,
    ) = viewModelScope.launch{
        crearCategoriaUseCase(
            parameters = CrearCategoriaConFamiliaParams(
                familiaId = familiaId,
                nombre = nombre,
                descripcion = descripcion
            )
        ).onSuccess { idCreated ->
            obtenerCategoriasPorFamiliaId(idCreated)
        }
    }

    fun crearSubcategoriaConCategoria(
        nombre: String,
        descripcion: String,
        categoriaId: Long,
    ) = viewModelScope.launch{
        crearSubcategoriaUseCase(
            parameters = CrearSubcategoriaConCategoriaParams(
                nombre = nombre,
                descripcion = descripcion,
                categoriaId = categoriaId
            )
        ).onSuccess { idCreated ->
            obtenerSubcategoriasPorCategoria(idCreated)
        }
    }
}