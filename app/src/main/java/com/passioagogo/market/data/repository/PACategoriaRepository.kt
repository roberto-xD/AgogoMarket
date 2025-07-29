package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import kotlinx.coroutines.flow.Flow

interface CategoriaRepository {
    fun obtenerCategoriasActivas(): Flow<List<CategoriaEntity>>
    fun obtenerCategoriasPorFamilia(familiaId: Long): Flow<List<CategoriaEntity>>
    suspend fun obtenerCategoriaPorId(id: Long): CategoriaEntity?
    suspend fun insertarCategoria(categoria: CategoriaEntity): Long
    suspend fun actualizarCategoria(categoria: CategoriaEntity)
    suspend fun eliminarCategoria(id: Long)
}