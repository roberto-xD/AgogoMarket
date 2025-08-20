package com.passioagogo.market.domain.usecase.categorias

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.data.repository.ICategoriaRepository
import com.passioagogo.market.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerCategoriasPorFamiliaUseCase @Inject constructor(
    private val categoriaRepository: ICategoriaRepository
) : UseCase<Long, Flow<List<Categoria>>>() {

    override suspend fun execute(parameters: Long): Flow<List<Categoria>> {
        return categoriaRepository.obtenerCategoriasPorFamilia(parameters)
    }
}