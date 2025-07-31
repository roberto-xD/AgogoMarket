package com.passioagogo.market.presentation.uiState

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.Proveedor

data class ProductosUiState(
    val productos: List<Producto> = emptyList(),
    val productosStockBajo: List<Producto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val proveedores: List<Proveedor> = emptyList(),
    val categoriaSeleccionada: Long? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val mensajeExito: String? = null
)