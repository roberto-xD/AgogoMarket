package com.passioagogo.market.domain.usecase.imagenes

import android.net.Uri
import com.passioagogo.market.ui.utils.ImageFileManager
import javax.inject.Inject

class SaveSharedImageUseCase @Inject constructor(
    private val imageFileManager: ImageFileManager,
//    private val imageRepository: ImageRepository // Asumiendo que ya existe
) {
    suspend fun execute(uri: Uri, productoId: Long? = null): Result<String> {
        return imageFileManager.saveImageFromUri(uri).fold(
            onSuccess = { imagePath ->
                // Aquí podrías guardar en Room si necesitas tracking
                // imageRepository.insertImage(imagePath, productoId)
                Result.success(imagePath)
            },
            onFailure = { Result.failure(it) }
        )
    }
}