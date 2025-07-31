package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject
import javax.inject.Singleton

data class EstablecerImagenPrincipalParams(
    val productoId: Long,
    val imagenId: Long
)

@Singleton
class EstablecerImagenPrincipalUseCase @Inject constructor(
    private val imagenRepository: IImagenRepository
) : UseCase<EstablecerImagenPrincipalParams, Unit>() {

    override suspend fun execute(parameters: EstablecerImagenPrincipalParams): Unit {
        when (val result = imagenRepository.establecerImagenPrincipal(
            productoId = parameters.productoId,
            imagenId = parameters.imagenId
        )) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}