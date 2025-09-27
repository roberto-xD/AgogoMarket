package com.passioagogo.market.presentation.view.models

import com.passioagogo.market.domain.bean.AtributoProducto

data class PAInfoModel(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val imagenes: List<String> = emptyList(),
    val codigoBarras: String?= null,
    val skuInterno: String = "",
    val precioCompra: Double = 0.0,
    val precioVenta: Double = 0.0,
    val cantidadActual: Int = 0,
    val cantidadMinima: Int = 0,
    val cantidadMaximaComprada: Int = 0,
    val fechaUltimaVenta: Long? = null,
    val fechaUltimaCompra: Long? = null,
    val activo: Boolean = true,
    val familia: String ?= null,
    val categorias: List<String> = emptyList(),
    val subcategorias: List<String> = emptyList(),
    val proveedores: List<String> = emptyList(),
    val atributos: List<AtributoProducto> = emptyList()
)