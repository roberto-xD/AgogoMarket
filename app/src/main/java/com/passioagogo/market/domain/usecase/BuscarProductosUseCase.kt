package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IProductoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class BuscarProductosParams(
    val query: String,
    val porNombre: Boolean = true,
    val porSku: Boolean = true,
    val porCodigoBarras: Boolean = true
)

@Singleton
class BuscarProductosUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<BuscarProductosParams, Flow<List<Producto>>>() {

    override suspend fun execute(parameters: BuscarProductosParams): Flow<List<Producto>> {
        return productoRepository.buscarProductos(parameters.query)
    }
}