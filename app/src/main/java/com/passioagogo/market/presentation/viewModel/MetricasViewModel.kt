package com.passioagogo.market.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import com.passioagogo.market.domain.usecase.ventas.GenerarReporteParams
import com.passioagogo.market.domain.usecase.ventas.GenerarReporteVentasUseCase
import com.passioagogo.market.domain.usecase.metricas.ObtenerMetricasInventarioUseCase
import com.passioagogo.market.presentation.uiState.MetricasUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetricasViewModel @Inject constructor(
    private val obtenerMetricasInventarioUseCase: ObtenerMetricasInventarioUseCase,
    private val generarReporteVentasUseCase: GenerarReporteVentasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricasUiState())
    val uiState: StateFlow<MetricasUiState> = _uiState.asStateFlow()

    init {
        cargarMetricas()
    }

    private fun cargarMetricas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            obtenerMetricasInventarioUseCase().onSuccess { metricas ->
                _uiState.update {
                    it.copy(
                        metricas = metricas,
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

    fun generarReporte(fechaInicio: Long, fechaFin: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(generandoReporte = true) }

            val params = GenerarReporteParams(
                fechaInicio = fechaInicio,
                fechaFin = fechaFin
            )

            generarReporteVentasUseCase(params).onSuccess { reporte ->
                _uiState.update {
                    it.copy(
                        reporteVentas = reporte,
                        generandoReporte = false
                    )
                }
            }.onError { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message,
                        generandoReporte = false
                    )
                }
            }
        }
    }

    fun actualizarMetricas() {
        cargarMetricas()
    }
}