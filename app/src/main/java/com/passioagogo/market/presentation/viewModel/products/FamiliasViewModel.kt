package com.passioagogo.market.presentation.viewModel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.familias.ObtenerFamiliasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FamiliasViewModel @Inject constructor(
    private val obtenerFamiliasUseCase: ObtenerFamiliasUseCase
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
}