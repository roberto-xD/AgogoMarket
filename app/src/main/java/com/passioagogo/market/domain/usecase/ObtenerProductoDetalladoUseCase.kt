package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.repository.IProductoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerProductoDetalladoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<Long, ProductoDetallado?>() {

    override suspend fun execute(parameters: Long): ProductoDetallado? {
        return productoRepository.obtenerProductoDetallado(parameters)
    }
}