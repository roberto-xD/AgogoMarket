package com.passioagogo.market.presentation.viewModel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.data.imports.GoogleSheetsImportService
import com.passioagogo.market.domain.bean.EstructuraValidacion
import com.passioagogo.market.domain.bean.ProductoImport
import com.passioagogo.market.domain.bean.ResultadoImportacion
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.producto.ImportarProductosDesdeGoogleSheetsUseCase
import com.passioagogo.market.domain.usecase.producto.ImportarProductosParams
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ImportacionViewModel @Inject constructor(
    private val importarProductosUseCase: ImportarProductosDesdeGoogleSheetsUseCase,
    private val googleSheetsService: GoogleSheetsImportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportacionUiState())
    val uiState: StateFlow<ImportacionUiState> = _uiState.asStateFlow()

    fun validarUrl(url: String) {
        if (url.isBlank()) {
            _uiState.update { it.copy(urlValida = false, mensajeValidacion = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    validandoUrl = true,
                    mensajeValidacion = null,
                    urlValida = false
                )
            }

            googleSheetsService.validarEstructura(url).onSuccess { validacion ->
                _uiState.update {
                    it.copy(
                        validandoUrl = false,
                        urlValida = validacion.esValida,
                        estructuraValidacion = validacion,
                        mensajeValidacion = if (validacion.esValida) {
                            "✅ Estructura válida - ${validacion.totalFilas} filas encontradas"
                        } else {
                            "❌ Estructura inválida - Revise los errores"
                        }
                    )
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        validandoUrl = false,
                        urlValida = false,
                        mensajeValidacion = "❌ Error: ${error.message}"
                    )
                }
            }
        }
    }

    fun obtenerVistaPrevia(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoVistaPrevia = true) }

            googleSheetsService.obtenerVistaPrevia(url, 10).onSuccess { productos ->
                _uiState.update {
                    it.copy(
                        cargandoVistaPrevia = false,
                        vistaPreviaProductos = productos
                    )
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        cargandoVistaPrevia = false,
                        errorMessage = "Error en vista previa: ${error.message}"
                    )
                }
            }
        }
    }

    fun iniciarImportacion(
        url: String,
        validarDuplicados: Boolean = true,
        saltearDuplicados: Boolean = true,
        actualizarExistentes: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    importando = true,
                    progresoImportacion = 0,
                    resultadoImportacion = null
                )
            }

            val params = ImportarProductosParams(
                spreadsheetUrl = url,
                validarDuplicados = validarDuplicados,
                saltearDuplicados = saltearDuplicados,
                actualizarExistentes = actualizarExistentes
            )

            importarProductosUseCase(params).onSuccess { resultado ->
                _uiState.update {
                    it.copy(
                        importando = false,
                        progresoImportacion = 100,
                        resultadoImportacion = resultado,
                        mensajeExito = "✅ Importación completada: ${resultado.exitosos}/${resultado.totalProcesados} productos"
                    )
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        importando = false,
                        errorMessage = "❌ Error en importación: ${error.message}"
                    )
                }
            }
        }
    }

    fun actualizarConfiguracion(config: ConfiguracionImportacion) {
        _uiState.update {
            it.copy(configuracionImportacion = config)
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

    fun limpiarResultados() {
        _uiState.update {
            it.copy(
                vistaPreviaProductos = emptyList(),
                resultadoImportacion = null,
                estructuraValidacion = null,
                urlValida = false,
                mensajeValidacion = null
            )
        }
    }
}

data class ImportacionUiState(
    val validandoUrl: Boolean = false,
    val urlValida: Boolean = false,
    val mensajeValidacion: String? = null,
    val estructuraValidacion: EstructuraValidacion? = null,
    val cargandoVistaPrevia: Boolean = false,
    val vistaPreviaProductos: List<ProductoImport> = emptyList(),
    val importando: Boolean = false,
    val progresoImportacion: Int = 0,
    val resultadoImportacion: ResultadoImportacion? = null,
    val configuracionImportacion: ConfiguracionImportacion = ConfiguracionImportacion(),
    val errorMessage: String? = null,
    val mensajeExito: String? = null
)

data class ConfiguracionImportacion(
    val validarDuplicados: Boolean = true,
    val saltearDuplicados: Boolean = true,
    val actualizarExistentes: Boolean = false
)