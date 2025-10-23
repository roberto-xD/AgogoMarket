package com.passioagogo.market.domain.usecase.subcategorias

import com.passioagogo.market.domain.bean.Subcategoria
import com.passioagogo.market.domain.repository.ISubcategoriaRepository
import com.passioagogo.market.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerSubcategoriaPorCategoriaUseCase @Inject constructor(
    private val subcategoriaRepository: ISubcategoriaRepository
) : UseCase<Long, Flow<List<Subcategoria>>>() {
    override suspend fun execute(parameters: Long): Flow<List<Subcategoria>> {
        return subcategoriaRepository.obtenerSubcategoriasPorCategoria(parameters)
    }
}