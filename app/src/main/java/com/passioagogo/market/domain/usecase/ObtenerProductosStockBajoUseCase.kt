package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IProductoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerProductosStockBajoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : NoParamsUseCase<Flow<List<Producto>>>() {

    override suspend fun execute(): Flow<List<Producto>> {
        return productoRepository.obtenerProductosConStockBajo()
    }
}