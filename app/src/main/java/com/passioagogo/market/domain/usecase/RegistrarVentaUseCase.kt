package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject
import javax.inject.Singleton

data class RegistrarVentaParams(
    val productoId: Long,
    val cantidad: Int,
    val precioVenta: Double? = null
)

@Singleton
class RegistrarVentaUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<RegistrarVentaParams, Unit>() {

    override suspend fun execute(parameters: RegistrarVentaParams): Unit {
        when (val result = productoRepository.registrarVenta(parameters.productoId, parameters.cantidad)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}