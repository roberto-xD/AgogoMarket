package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EliminarProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<Long, Unit>() {

    override suspend fun execute(parameters: Long): Unit {
        when (val result = productoRepository.eliminarProducto(parameters)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}