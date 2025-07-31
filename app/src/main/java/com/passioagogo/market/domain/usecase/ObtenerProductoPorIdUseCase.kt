package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IProductoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerProductoPorIdUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<Long, Producto?>() {

    override suspend fun execute(parameters: Long): Producto? {
        return productoRepository.obtenerProductoPorId(parameters)
    }
}