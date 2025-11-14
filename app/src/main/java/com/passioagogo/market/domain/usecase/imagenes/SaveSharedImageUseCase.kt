package com.passioagogo.market.domain.usecase.imagenes

import android.R.attr.path
import android.net.Uri
import android.util.Log
import com.google.api.Context
import com.passioagogo.market.data.repository.StorageRepository
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.ui.utils.ImageFileManager
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class SaveSharedImageUseCase @Inject constructor(
    private val imageFileManager: ImageFileManager,
    private val imagenRepository: IImagenRepository,
    private val storageRepository: StorageRepository,
) {
    suspend fun execute(uri: Uri, productoId: Long? = null): Result<String> {
        return imageFileManager.saveImageFromUri(uri).fold(
            onSuccess = { imagePath ->
                val fileName = File(imagePath).name
                Log.i(TAG_PG,"current filename: $fileName")
                storageRepository.uploadImageFromUri(
                    fileName = fileName,
                    uri = uri
                ).fold(
                    onSuccess = {
                        Log.i(TAG_PG,"Imagen subida correctamente: $it")
                    }, onFailure = {
                        Log.i(TAG_PG,"Error al subir imagen: $it")
                    }
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