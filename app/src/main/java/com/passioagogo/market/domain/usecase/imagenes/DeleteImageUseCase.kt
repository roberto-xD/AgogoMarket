package com.passioagogo.market.domain.usecase.imagenes

import com.passioagogo.market.ui.utils.ImageFileManager
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val imageFileManager: ImageFileManager,
//    private val imageRepository: ImageRepository
) {
    suspend fun execute(imagePath: String): Result<Unit> {
        return try {
            // Eliminar de Room si existe registro
            // imageRepository.deleteImageByPath(imagePath)

            // Eliminar archivo físico
            val deleted = imageFileManager.deleteImage(imagePath)
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