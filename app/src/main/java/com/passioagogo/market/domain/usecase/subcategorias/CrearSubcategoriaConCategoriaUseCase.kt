package com.passioagogo.market.domain.usecase.subcategorias

import com.passioagogo.market.data.repository.ICategoriaRepository
import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.bean.Subcategoria
import com.passioagogo.market.domain.repository.ISubcategoriaRepository
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import jakarta.inject.Inject
import javax.inject.Singleton

data class CrearSubcategoriaConCategoriaParams(
    val nombre: String,
    val descripcion: String?,
    val categoriaId: Long
)

@Singleton
class CrearSubcategoriaConCategoriaUseCase @Inject constructor(
    private val subcategoriaRepository: ISubcategoriaRepository
) : UseCase<CrearSubcategoriaConCategoriaParams, Long>() {

    override suspend fun execute(parameters: CrearSubcategoriaConCategoriaParams): Long {
        if (parameters.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre de la subcategoría no puede estar vacío")
        }

        val subcategoria = Subcategoria(
            nombre = parameters.nombre,
            descripcion = parameters.descripcion,
            categoriaId = parameters.categoriaId
        )

        return when (val result = subcategoriaRepository.guardarSubcategoria(subcategoria)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}