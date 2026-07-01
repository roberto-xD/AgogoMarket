package com.passioagogo.market.presentation.viewModel.proveedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.compras.CrearProveedorUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProveedoresUseCase
import com.passioagogo.market.presentation.view.components.ProveedorModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProveedoresViewModel @Inject constructor(
    val obtenerProveedoresUseCase: ObtenerProveedoresUseCase,
    val crearProveedorUseCase: CrearProveedorUseCase
): ViewModel(){
    private val _uiState = MutableStateFlow(ProveedorUiState())
    val uiState: StateFlow<ProveedorUiState> = _uiState.asStateFlow()


    fun actualizarItem(
        proveedor: ProveedorModel
    ){
        _uiState.update {
            it.copy(
                proveedorModel = proveedor,
                erroresValidacion = emptyMap()
            )
        }
    }

    fun guardarProveedor(
        proveedorModel : ProveedorModel
    ) = viewModelScope.launch {
        val errores = proveedorModel.validar()
        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(erroresValidacion = errores)
            }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, erroresValidacion = emptyMap()) }

        crearProveedorUseCase.invoke(proveedorModel)
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensajeExito = "Proveedor guardado exitosamente"
                    )
                }
            }
            .onError { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al guardar proveedor"
                    )
                }
            }
    }

    fun obtenerProveedores(

    ) = viewModelScope.launch{
        obtenerProveedoresUseCase.invoke()
            .onSuccess {  }
            .onError {  }
    }
}

data class ProveedorUiState(
    val proveedorModel: ProveedorModel = ProveedorModel(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val mensajeExito: String? = null,
    val erroresValidacion: Map<String, String> = emptyMap(),
)
