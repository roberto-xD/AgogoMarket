package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity
import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagenRepositoryImpl @Inject constructor(
    private val productoImagenDao: ProductoImagenDao
) : IImagenRepository {
    override suspend fun obtenerTodasLasImagenes(): List<ImagenProducto> {
        return productoImagenDao.obtenerTodasLasImagenes().map { entity ->
            ImagenProducto(
                id = entity.id,
                productoId = entity.productoId,
                rutaImagen = entity.rutaImagen,
                esPrincipal = entity.esPrincipal,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    override suspend fun obtenerImagenesPorProducto(productoId: Long): List<ImagenProducto> {
        return productoImagenDao.obtenerImagenesPorProducto(productoId).map { entity ->
            ImagenProducto(
                id = entity.id,
                productoId = entity.productoId,
                rutaImagen = entity.rutaImagen,
                orden = entity.orden,
                esPrincipal = entity.esPrincipal,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    override suspend fun obtenerImagenPrincipal(productoId: Long): ImagenProducto? {
        return productoImagenDao.obtenerImagenPrincipal(productoId)?.let { entity ->
            ImagenProducto(
                id = entity.id,
                productoId = entity.productoId,
                rutaImagen = entity.rutaImagen,
                orden = entity.orden,
                esPrincipal = entity.esPrincipal,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    override suspend fun guardarImagen(
        productoId: Long,
        rutaImagen: String,
        esPrincipal: Boolean
    ): PADomainState<Long> {
        return try {
            val imagenEntity = ProductoImagenEntity(
                productoId = productoId,
                rutaImagen = rutaImagen,
                esPrincipal = esPrincipal
            )
            val id = productoImagenDao.insertarImagen(imagenEntity)
            PADomainState.Success(id)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarImagen(imagen: ImagenProducto): PADomainState<Unit> {
        return try {
            val entity = ProductoImagenEntity(
                id = imagen.id,
                productoId = imagen.productoId,
                rutaImagen = imagen.rutaImagen,
                orden = imagen.orden,
                esPrincipal = imagen.esPrincipal,
                fechaCreacion = imagen.fechaCreacion
            )
            productoImagenDao.actualizarImagen(entity)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarImagen(id: Long): PADomainState<Unit> {
        return try {
            productoImagenDao.eliminarImagen(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun establecerImagenPrincipal(productoId: Long, imagenId: Long): PADomainState<Unit> {
        return try {
            // Primero quitar la marca principal de todas las imágenes del producto
            val imagenes = productoImagenDao.obtenerImagenesPorProducto(productoId)
            imagenes.forEach { imagen ->
                if (imagen.esPrincipal) {
                    productoImagenDao.actualizarImagen(imagen.copy(esPrincipal = false))
                }
            }

            // Establecer la nueva imagen como principal
            val imagenPrincipal = imagenes.find { it.id == imagenId }
            imagenPrincipal?.let {
                productoImagenDao.actualizarImagen(it.copy(esPrincipal = true))
            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarImagenDeProducto(
        productoId: Long,
        imagenId: Long
    ): PADomainState<Unit> {
        return try {
            val imagenes = productoImagenDao.obtenerImagenesPorProducto(productoId)
            val toDelete = imagenes.find { it.id == imagenId }
            toDelete?.let {
                productoImagenDao.eliminarImagen(it.id)
            }
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
}