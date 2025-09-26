package com.passioagogo.market.presentation.view.models

data class PAEditInfoModel(
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
    val proveedorPrincipalId: String? = null,
    val fechaUltimaVenta: Long? = null,
    val fechaUltimaCompra: Long? = null,
    val activo: Boolean = true,
)