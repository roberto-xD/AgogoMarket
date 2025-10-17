package com.passioagogo.market.domain.model.venta

import com.passioagogo.market.data.local.entity.venta.VentaProductoEntity

data class ProductoVenta(
    val productoId: Long,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val stockDisponible: Int,
    val subtotal: Double = cantidad * precioUnitario
)

fun ProductoVenta.toEntity(ventaId: Long) = VentaProductoEntity(
    ventaId = ventaId,
    productoId = productoId,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

enum class EstadoVenta {
    PENDIENTE,
    LIQUIDADA,
    CANCELADA
}