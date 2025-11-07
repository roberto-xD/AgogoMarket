package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Producto Familia
@Serializable
data class ProductoFamiliaDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("familia_id")
    val familiaId: String
)

// Producto Categoría
@Serializable
data class ProductoCategoriaDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("categoria_id")
    val categoriaId: String,

    @SerialName("es_principal")
    val esPrincipal: Boolean
)

// Producto Subcategoría
@Serializable
data class ProductoSubcategoriaDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("subcategoria_id")
    val subcategoriaId: String
)

// Producto Proveedor
@Serializable
data class ProductoProveedorDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("proveedor_id")
    val proveedorId: String,

    @SerialName("precio_compra")
    val precioCompra: Double,

    @SerialName("fecha_ultima_compra")
    val fechaUltimaCompra: String?,

    @SerialName("activo")
    val activo: Boolean
)