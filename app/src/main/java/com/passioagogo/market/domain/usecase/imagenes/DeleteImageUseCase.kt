package com.passioagogo.market.domain.usecase.imagenes

import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.ui.utils.ImageFileManager
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val imageFileManager: ImageFileManager,
    private val imagenRepository: IImagenRepository
) {
    suspend fun execute(
        imageModel : ImagenProducto,
    ): Result<Unit> {
        return try {
            // Eliminar de Room si existe registro
            if(imageModel.productoId != 0L && imageModel.id != 0L){
                imagenRepository.eliminarImagenDeProducto(productoId = imageModel.productoId, imagenId = imageModel.id)
            }

            // Eliminar archivo físico
            val deleted = imageFileManager.deleteImage(imageModel.rutaImagen)
            if (deleted) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo eliminar el archivo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}