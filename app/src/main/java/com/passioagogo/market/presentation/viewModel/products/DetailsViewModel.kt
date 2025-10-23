package com.passioagogo.market.presentation.viewModel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaParams
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaUseCase
import com.passioagogo.market.domain.usecase.familias.ObtenerFamiliasUseCase
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaParams
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val obtenerFamiliasUseCase: ObtenerFamiliasUseCase,
    private val crearCategoriaUseCase: CrearCategoriaConFamiliaUseCase,
    private val crearSubcategoriaUseCase: CrearSubcategoriaConCategoriaUseCase,
): ViewModel() {
    private val _familias = MutableStateFlow<List<Familia>>(emptyList())
    val familias : StateFlow<List<Familia>> = _familias.asStateFlow()

    init {
        obtenerFamilias()
    }
    fun obtenerFamilias() {
        viewModelScope.launch {
            obtenerFamiliasUseCase().onSuccess { familias ->
                familias.collect {
                    _familias.value = it
                }
            }.onError {

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
        )
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
        )
    }
}