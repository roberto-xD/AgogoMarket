package com.passioagogo.market.domain.usecase.categorias

import com.passioagogo.market.domain.repository.ICategoriaRepository
import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import jakarta.inject.Inject
import javax.inject.Singleton

data class CrearCategoriaConFamiliaParams(
    val nombre: String,
    val descripcion: String?,
    val familiaId: Long
)

@Singleton
class CrearCategoriaConFamiliaUseCase @Inject constructor(
    private val categoriaRepository: ICategoriaRepository
) : UseCase<CrearCategoriaConFamiliaParams, Long>() {

    override suspend fun execute(parameters: CrearCategoriaConFamiliaParams): Long {
        if (parameters.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre de la categoría no puede estar vacío")
        }

        val categoria = Categoria(
            nombre = parameters.nombre,
            descripcion = parameters.descripcion,
            familiaId = parameters.familiaId
        )

        return when (val result = categoriaRepository.guardarCategoria(categoria)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}