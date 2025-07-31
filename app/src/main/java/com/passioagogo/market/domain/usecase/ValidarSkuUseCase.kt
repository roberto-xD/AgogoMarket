package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.IProductoRepository
import javax.inject.Inject
import javax.inject.Singleton

data class ValidarSkuParams(
    val sku: String,
    val productoIdExcluir: Long? = null // Para edición de productos
)

@Singleton
class ValidarSkuUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<ValidarSkuParams, Boolean>() {

    override suspend fun execute(parameters: ValidarSkuParams): Boolean {
        if (parameters.sku.isBlank()) return false

        val productoExistente = productoRepository.obtenerProductoPorSku(parameters.sku)
        return productoExistente == null ||
                productoExistente.id == parameters.productoIdExcluir
    }
}