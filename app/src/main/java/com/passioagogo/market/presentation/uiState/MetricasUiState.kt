package com.passioagogo.market.presentation.uiState

import com.passioagogo.market.domain.usecase.MetricasInventario
import com.passioagogo.market.domain.usecase.ReporteVentas

data class MetricasUiState(
    val metricas: MetricasInventario? = null,
    val reporteVentas: ReporteVentas? = null,
    val isLoading: Boolean = false,
    val generandoReporte: Boolean = false,
    val errorMessage: String? = null
)