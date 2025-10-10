package com.passioagogo.market.domain.usecase.imagenes

import android.net.Uri
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.ui.utils.ImageFileManager
import javax.inject.Inject

class SaveSharedImageUseCase @Inject constructor(
    private val imageFileManager: ImageFileManager,
    private val imagenRepository: IImagenRepository
) {
    suspend fun execute(uri: Uri, productoId: Long? = null): Result<String> {
        return imageFileManager.saveImageFromUri(uri).fold(
            onSuccess = { imagePath ->
                productoId?.let {
                    imagenRepository.guardarImagen(productoId = it, rutaImagen = imagePath, esPrincipal = false)
                }
                Result.success(imagePath)
            },
            onFailure = { Result.failure(it) }
        )
    }
}