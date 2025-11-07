package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow

interface ICategoriaRepository {
    suspend fun obtenerTodasLasCategorias(): Flow<List<Categoria>>
    suspend fun obtenerCategoriasPorFamiliaId(familiaId: Long): Flow<List<Categoria>>
    suspend fun obtenerCategoriaPorId(id: Long): Categoria?
    suspend fun guardarCategoria(categoria: Categoria): PADomainState<Long>
    suspend fun actualizarCategoria(categoria: Categoria): PADomainState<Unit>
    suspend fun eliminarCategoria(id: Long): PADomainState<Unit>
    suspend fun sincronizarConSupabase(): PADomainState<Unit>
}