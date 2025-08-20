package com.passioagogo.market.domain.usecase.categorias

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.data.repository.ICategoriaRepository
import com.passioagogo.market.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerCategoriasUseCase @Inject constructor(
    private val categoriaRepository: ICategoriaRepository
) : NoParamsUseCase<Flow<List<Categoria>>>() {

    override suspend fun execute(): Flow<List<Categoria>> {
        return categoriaRepository.obtenerTodasLasCategorias()
    }
}