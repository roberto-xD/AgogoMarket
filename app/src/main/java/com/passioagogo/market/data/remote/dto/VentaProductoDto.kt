package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VentaProductoDto(
    @SerialName("id")
    val id: String,

    @SerialName("venta_id")
    val ventaId: String,

    @SerialName("producto_id")
    val productoId: String,

    @SerialName("cantidad")
    val cantidad: Int,

    @SerialName("precio_unitario")
    val precioUnitario: Double,

    @SerialName("subtotal")
    val subtotal: Double
)

@Serializable
data class CreateVentaProductoDto(
    @SerialName("venta_id")
    val ventaId: String,

    @SerialName("producto_id")
    val productoId: String,

    @SerialName("cantidad")
    val cantidad: Int,

    @SerialName("precio_unitario")
    val precioUnitario: Double,

    @SerialName("subtotal")
    val subtotal: Double
)