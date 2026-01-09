package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.domain.bean.Subcategoria
import com.passioagogo.market.domain.repository.ISubcategoriaRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubcategoriaRepositoryDomainImpl @Inject constructor(
    private val subcategoriaDao: SubcategoriaDao,
) : ISubcategoriaRepository {

    companion object {
        private const val TAG = "SubcategoriaRepository"
    }

    override suspend fun obtenerTodasLasSubcategorias(): Flow<List<Subcategoria>> {
        return subcategoriaDao.obtenerSubcategoriasActivas().map { entities ->
            entities.map { Subcategoria.fromEntity(it) }
        }
    }

    override suspend fun obtenerSubcategoriasPorCategoria(categoriaId: Long): Flow<List<Subcategoria>> {
        return subcategoriaDao.obtenerSubcategoriasPorCategoria(categoriaId).map { entities ->
            entities.map { Subcategoria.fromEntity(it) }
        }
    }

    override suspend fun obtenerSubcategoriaPorId(id: Long): Subcategoria? {
        return subcategoriaDao.obtenerSubcategoriaPorId(id)?.let { Subcategoria.fromEntity(it) }
    }

    override suspend fun guardarSubcategoria(subcategoria: Subcategoria): PADomainState<Long> {
        return try {
            val entity = subcategoria.toEntity()
            val id = subcategoriaDao.insertarSubcategoria(entity)
            PADomainState.Success(id)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarSubcategoria(subcategoria: Subcategoria): PADomainState<Unit> {
        return try {
            val entity = subcategoria.toEntity()
            subcategoriaDao.actualizarSubcategoria(entity)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarSubcategoria(id: Long): PADomainState<Unit> {
        return try {
            subcategoriaDao.eliminarSubcategoria(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
}