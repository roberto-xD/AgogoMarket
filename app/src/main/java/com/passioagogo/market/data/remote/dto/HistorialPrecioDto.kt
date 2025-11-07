package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistorialPrecioDto(
    @SerialName("id")
    val id: String,

    @SerialName("producto_id")
    val productoId: String,

    @SerialName("precio_compra_anterior")
    val precioCompraAnterior: Double,

    @SerialName("precio_venta_anterior")
    val precioVentaAnterior: Double,

    @SerialName("precio_compra_nuevo")
    val precioCompraNuevo: Double,

    @SerialName("precio_venta_nuevo")
    val precioVentaNuevo: Double,

    @SerialName("motivo")
    val motivo: String?,

    @SerialName("fecha_cambio")
    val fechaCambio: String
)

@Serializable
data class CreateHistorialPrecioDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("precio_compra_anterior")
    val precioCompraAnterior: Double,

    @SerialName("precio_venta_anterior")
    val precioVentaAnterior: Double,

    @SerialName("precio_compra_nuevo")
    val precioCompraNuevo: Double,

    @SerialName("precio_venta_nuevo")
    val precioVentaNuevo: Double,

    @SerialName("motivo")
    val motivo: String? = null
)