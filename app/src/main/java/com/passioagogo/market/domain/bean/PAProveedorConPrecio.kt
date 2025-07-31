package com.passioagogo.market.domain.bean

data class ProveedorConPrecio(
    val proveedor: Proveedor,
    val precioCompra: Double,
    val fechaUltimaCompra: Long?,
    val activo: Boolean = true
)