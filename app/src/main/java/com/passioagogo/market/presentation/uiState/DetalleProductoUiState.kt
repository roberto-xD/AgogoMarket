package com.passioagogo.market.presentation.uiState

import com.passioagogo.market.domain.bean.ProductoDetallado

data class DetalleProductoUiState(
    val productoDetallado: ProductoDetallado? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val mensajeExito: String? = null,
    val skuValido: Boolean? = null,
    val codigoBarrasValido: Boolean? = null
)