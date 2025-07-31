package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject
import javax.inject.Singleton

data class RegistrarCompraParams(
    val productoId: Long,
    val cantidad: Int,
    val precioCompra: Double? = null,
    val proveedorId: Long? = null
)

@Singleton
class RegistrarCompraUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<RegistrarCompraParams, Unit>() {

    override suspend fun execute(parameters: RegistrarCompraParams): Unit {
        when (val result = productoRepository.registrarCompra(parameters.productoId, parameters.cantidad)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}