package com.passioagogo.market.presentation.view.models

import com.passioagogo.market.domain.bean.ImagenProducto

data class PAImageModel(
    val id: Long = 0,
    val idProduct: Long = 0,
    val orden: Int,
    val rutaImagen: String,
    val esPrincipal: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
){
    fun toImagenProducto() = ImagenProducto(
        id = id,
        productoId = idProduct,
        orden = orden,
        rutaImagen = rutaImagen,
        fechaCreacion = fechaCreacion,
        esPrincipal = esPrincipal
    )
}