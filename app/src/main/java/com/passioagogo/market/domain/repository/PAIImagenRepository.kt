package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.state.PADomainState

interface IImagenRepository {
    suspend fun obtenerTodasLasImagenes(): List<ImagenProducto>
    suspend fun obtenerImagenesPorProducto(productoId: Long): List<ImagenProducto>
    suspend fun obtenerImagenPrincipal(productoId: Long): ImagenProducto?
    suspend fun guardarImagen(productoId: Long, rutaImagen: String, esPrincipal: Boolean = false): PADomainState<Long>
    suspend fun actualizarImagen(imagen: ImagenProducto): PADomainState<Unit>
    suspend fun eliminarImagen(id: Long): PADomainState<Unit>
    suspend fun establecerImagenPrincipal(productoId: Long, imagenId: Long): PADomainState<Unit>

    suspend fun eliminarImagenDeProducto(productoId: Long, imagenId: Long): PADomainState<Unit>
}