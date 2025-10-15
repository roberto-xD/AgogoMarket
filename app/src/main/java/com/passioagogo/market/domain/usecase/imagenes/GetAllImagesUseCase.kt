package com.passioagogo.market.domain.usecase.imagenes

import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.repository.IImagenRepository
import javax.inject.Inject

class GetAllImagesUseCase @Inject constructor(
    private val imageRepository: IImagenRepository
) {
    suspend fun execute(): List<ImagenProducto>? {
        // Retorna las rutas de las imágenes desde Room o directorio
        return imageRepository.obtenerTodasLasImagenes()
    }
}