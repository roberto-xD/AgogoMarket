package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoImagenDto(
    @SerialName("id")
    val id: String,

    @SerialName("producto_id")
    val productoId: String,

    @SerialName("ruta_imagen")
    val rutaImagen: String,

    @SerialName("orden")
    val orden: Int,

    @SerialName("es_principal")
    val esPrincipal: Boolean,

    @SerialName("fecha_creacion")
    val fechaCreacion: String
)

@Serializable
data class CreateProductoImagenDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("ruta_imagen")
    val rutaImagen: String,

    @SerialName("orden")
    val orden: Int = 0,

    @SerialName("es_principal")
    val esPrincipal: Boolean = false
)