package com.passioagogo.market.domain.usecase.imagenes

import android.net.Uri
import com.google.api.Context
import com.passioagogo.market.data.repository.StorageRepository
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.ui.utils.ImageFileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveSharedImageUseCase @Inject constructor(
    private val imageFileManager: ImageFileManager,
    private val imagenRepository: IImagenRepository,
    private val storageRepository: StorageRepository,
) {
    suspend fun execute(uri: Uri, productoId: Long? = null): Result<String> {
        return imageFileManager.saveImageFromUri(uri).fold(
            onSuccess = { imagePath ->
                storageRepository.uploadImageFromUri(
                    fileName = imagePath,
                    uri = uri
                )
                productoId?.let {
                    imagenRepository.guardarImagen(productoId = it, rutaImagen = imagePath, esPrincipal = false)
                }
                Result.success(imagePath)
            },
            onFailure = { Result.failure(it) }
        )
    }
}