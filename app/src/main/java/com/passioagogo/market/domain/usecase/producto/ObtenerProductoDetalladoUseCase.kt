package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.usecase.base.UseCase
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