package com.passioagogo.market.domain.usecase.imagenes

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllImagesUseCase @Inject constructor(
//    private val imageRepository: ImageRepository
) {
    suspend fun execute(): Flow<List<String>>? {
        // Retorna las rutas de las imágenes desde Room o directorio
        return null//imageRepository.getAllImagePaths()
    }
}