package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String?,

    @SerialName("codigo_barras")
    val codigoBarras: String?,

    @SerialName("sku_interno")
    val skuInterno: String,

    @SerialName("precio_compra")
    val precioCompra: Double,

    @SerialName("precio_venta")
    val precioVenta: Double,

    @SerialName("cantidad_actual")
    val cantidadActual: Int,

    @SerialName("cantidad_minima")
    val cantidadMinima: Int,

    @SerialName("cantidad_maxima_comprada")
    val cantidadMaximaComprada: Int,

    @SerialName("proveedor_principal_id")
    val proveedorPrincipalId: String?,

    @SerialName("color")
    val color: String?,

    @SerialName("fecha_ultima_venta")
    val fechaUltimaVenta: String?,

    @SerialName("fecha_ultima_compra")
    val fechaUltimaCompra: String?,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,

    @SerialName("fecha_actualizacion")
    val fechaActualizacion: String,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateProductoDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String? = null,

    @SerialName("codigo_barras")
    val codigoBarras: String? = null,

    @SerialName("sku_interno")
    val skuInterno: String,

    @SerialName("precio_compra")
    val precioCompra: Double = 0.0,

    @SerialName("precio_venta")
    val precioVenta: Double = 0.0,

    @SerialName("cantidad_actual")
    val cantidadActual: Int = 0,

    @SerialName("cantidad_minima")
    val cantidadMinima: Int = 0,

    @SerialName("cantidad_maxima_comprada")
    val cantidadMaximaComprada: Int = 0,

    @SerialName("proveedor_principal_id")
    val proveedorPrincipalId: String? = null,

    @SerialName("color")
    val color: String? = null,

    @SerialName("activo")
    val activo: Boolean = true
)