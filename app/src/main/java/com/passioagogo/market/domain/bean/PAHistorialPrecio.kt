package com.passioagogo.market.domain.bean

data class HistorialPrecio(
    val id: Long = 0,
    val productoId: Long,
    val precioCompraAnterior: Double,
    val precioVentaAnterior: Double,
    val precioCompraNuevo: Double,
    val precioVentaNuevo: Double,
    val motivo: String?,
    val fechaCambio: Long = System.currentTimeMillis()
)
