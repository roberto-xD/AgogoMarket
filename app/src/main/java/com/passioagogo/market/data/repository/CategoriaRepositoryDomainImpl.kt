package com.passioagogo.market.data.repository

import android.util.Log
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.repository.ICategoriaRepository
import com.passioagogo.market.domain.state.PADomainState
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriaRepositoryDomainImpl @Inject constructor(
    private val categoriaDao: CategoriaDao,
) : ICategoriaRepository {

    companion object {
        private const val TAG = "CategoriaRepository"
    }

    // ========================================
    // OPERACIONES LOCALES
    // ========================================

    override suspend fun obtenerTodasLasCategorias(): Flow<List<Categoria>> {
        return categoriaDao.obtenerCategoriasActivas().map { entities ->
            entities.map { Categoria.fromEntity(it) }
        }
    }

    override suspend fun obtenerCategoriasPorFamiliaId(familiaId: Long): Flow<List<Categoria>> {
        return categoriaDao.obtenerCategoriasPorFamiliaId(familiaId).map { entities ->
            entities.map { Categoria.fromEntity(it) }
        }
    }

    override suspend fun obtenerCategoriaPorId(id: Long): Categoria? {
        return categoriaDao.obtenerCategoriaPorId(id)?.let { Categoria.fromEntity(it) }
    }

    override suspend fun guardarCategoria(categoria: Categoria): PADomainState<Long> {
        return try {
            val entity = categoria.toEntity()
            val id = categoriaDao.insertarCategoria(entity)
            PADomainState.Success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar categoría: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarCategoria(categoria: Categoria): PADomainState<Unit> {
        return try {
            val entity = categoria.toEntity()
            categoriaDao.actualizarCategoria(entity)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar categoría: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarCategoria(id: Long): PADomainState<Unit> {
        return try {
            categoriaDao.eliminarCategoria(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar categoría: ${e.message}")
            PADomainState.Error(e)
        }
    }
}