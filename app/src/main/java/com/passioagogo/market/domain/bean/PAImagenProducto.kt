package com.passioagogo.market.domain.bean

data class ImagenProducto(
    val id: Long = 0,
    val rutaImagen: String,
    val orden: Int = 0,
    val esPrincipal: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)