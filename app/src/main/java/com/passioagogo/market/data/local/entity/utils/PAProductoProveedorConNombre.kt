package com.passioagogo.market.data.local.entity.utils

data class ProductoProveedorConNombre(
    val productoId: Long,
    val proveedorId: Long,
    val precioCompra: Double,
    val fechaUltimaCompra: Long?,
    val activo: Boolean,
    val nombreProveedor: String
)
