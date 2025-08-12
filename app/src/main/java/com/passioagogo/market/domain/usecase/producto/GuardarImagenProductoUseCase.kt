package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import javax.inject.Inject
import javax.inject.Singleton

data class GuardarImagenParams(
    val productoId: Long,
    val rutaImagen: String,
    val esPrincipal: Boolean = false
)

@Singleton
class GuardarImagenProductoUseCase @Inject constructor(
    private val imagenRepository: IImagenRepository
) : UseCase<GuardarImagenParams, Long>() {

    override suspend fun execute(parameters: GuardarImagenParams): Long {
        return when (val result = imagenRepository.guardarImagen(
            productoId = parameters.productoId,
            rutaImagen = parameters.rutaImagen,
            esPrincipal = parameters.esPrincipal
        )) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}