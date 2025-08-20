package com.passioagogo.market.data.implementation

import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.data.repository.ICategoriaRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriaRepositoryDomainImpl @Inject constructor(
    private val categoriaDao: CategoriaDao
) : ICategoriaRepository {

    override suspend fun obtenerTodasLasCategorias(): Flow<List<Categoria>> {
        return categoriaDao.obtenerCategoriasActivas().map { entities ->
            entities.map { Categoria.fromEntity(it) }
        }
    }

    override suspend fun obtenerCategoriasPorFamilia(familiaId: Long): Flow<List<Categoria>> {
        return categoriaDao.obtenerCategoriasPorFamilia(familiaId).map { entities ->
            entities.map { Categoria.fromEntity(it) }
        }
    }

    override suspend fun obtenerCategoriaPorId(id: Long): Categoria? {
        return categoriaDao.obtenerCategoriaPorId(id)?.let { Categoria.fromEntity(it) }
    }

    override suspend fun guardarCategoria(categoria: Categoria): PADomainState<Long> {
        return try {
            val id = categoriaDao.insertarCategoria(categoria.toEntity())
            PADomainState.Success(id)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarCategoria(categoria: Categoria): PADomainState<Unit> {
        return try {
            categoriaDao.actualizarCategoria(categoria.toEntity())
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarCategoria(id: Long): PADomainState<Unit> {
        return try {
            categoriaDao.eliminarCategoria(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
}