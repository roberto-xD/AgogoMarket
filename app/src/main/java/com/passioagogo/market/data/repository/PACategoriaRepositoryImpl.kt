package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriaRepositoryImpl @Inject constructor(
    private val categoriaDao: CategoriaDao
) : CategoriaRepository {

    override fun obtenerCategoriasActivas(): Flow<List<CategoriaEntity>> =
        categoriaDao.obtenerCategoriasActivas()

    override fun obtenerCategoriasPorFamilia(familiaId: Long): Flow<List<CategoriaEntity>> =
        categoriaDao.obtenerCategoriasPorFamilia(familiaId)

    override suspend fun obtenerCategoriaPorId(id: Long): CategoriaEntity? =
        categoriaDao.obtenerCategoriaPorId(id)

    override suspend fun insertarCategoria(categoria: CategoriaEntity): Long =
        categoriaDao.insertarCategoria(categoria)

    override suspend fun actualizarCategoria(categoria: CategoriaEntity) =
        categoriaDao.actualizarCategoria(categoria)

    override suspend fun eliminarCategoria(id: Long) =
        categoriaDao.eliminarCategoria(id)
}