package com.passioagogo.market.domain.bean

import com.passioagogo.market.presentation.view.models.PAImageModel

data class ImagenProducto(
    val id: Long = 0,
    val productoId: Long = 0,
    val rutaImagen: String,
    val orden: Int = 0,
    val esPrincipal: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
){
    fun toImageProductModel() = PAImageModel(
        id = id,
        idProduct = productoId,
        orden = orden,
        rutaImagen = rutaImagen,
        esPrincipal = esPrincipal,
        fechaCreacion = fechaCreacion,
    )
}