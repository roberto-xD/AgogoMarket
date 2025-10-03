package com.passioagogo.market.domain.usecase.familias

import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.repository.IFamiliaRepository
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import jakarta.inject.Inject
import javax.inject.Singleton

data class CrearFamiliaParams(
    val nombre: String,
    val descripcion: String
)

@Singleton
class CrearFamiliaUseCase  @Inject constructor(
    private val familiaRepository: IFamiliaRepository
) : UseCase<CrearFamiliaParams, Long>() {
    override suspend fun execute(parameters: CrearFamiliaParams): Long {
        if (parameters.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre de la familia no puede estar vacío")
        }

        val familia = Familia(
            nombre = parameters.nombre,
            descripcion = parameters.descripcion
        )
        val result = familiaRepository.guardarFamilia(familia)
        return when (result) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}