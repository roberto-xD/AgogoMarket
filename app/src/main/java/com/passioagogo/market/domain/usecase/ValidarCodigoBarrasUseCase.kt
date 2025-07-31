package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.IProductoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValidarCodigoBarrasUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<String, Boolean>() {

    override suspend fun execute(parameters: String): Boolean {
        if (parameters.isBlank()) return true // Código vacío es válido

        // Verificar que no existe otro producto con el mismo código
        val productoExistente = productoRepository.obtenerProductoPorCodigoBarras(parameters)
        return productoExistente == null
    }
}