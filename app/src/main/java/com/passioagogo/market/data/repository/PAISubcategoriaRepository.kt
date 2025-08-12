package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.Subcategoria
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow

interface ISubcategoriaRepository {
    suspend fun obtenerTodasLasSubcategorias(): Flow<List<Subcategoria>>
    suspend fun obtenerSubcategoriasPorCategoria(categoriaId: Long): Flow<List<Subcategoria>>
    suspend fun obtenerSubcategoriaPorId(id: Long): Subcategoria?
    suspend fun guardarSubcategoria(subcategoria: Subcategoria): PADomainState<Long>
    suspend fun actualizarSubcategoria(subcategoria: Subcategoria): PADomainState<Unit>
    suspend fun eliminarSubcategoria(id: Long): PADomainState<Unit>
}