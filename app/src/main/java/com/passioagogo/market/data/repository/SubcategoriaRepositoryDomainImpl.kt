package com.passioagogo.market.data.repository

import android.util.Log
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
//import com.passioagogo.market.data.mapper.toCreateDto
//import com.passioagogo.market.data.mapper.toEntity
import com.passioagogo.market.data.remote.dto.SubcategoriaDto
import com.passioagogo.market.data.repository.sync.SyncHelper
import com.passioagogo.market.domain.bean.Subcategoria
import com.passioagogo.market.domain.repository.ISubcategoriaRepository
import com.passioagogo.market.domain.state.PADomainState
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubcategoriaRepositoryDomainImpl @Inject constructor(
    private val subcategoriaDao: SubcategoriaDao,
    private val categoriaDao: CategoriaDao,
    private val auth: Auth,
    private val postgrest: Postgrest
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
            val userId = auth.currentUserOrNull()?.id

            val categoria = categoriaDao.obtenerCategoriaPorId(subcategoria.categoriaId)
//            val categoriaRemoteId = categoria?.remoteId

//            val entity = subcategoria.toEntity().copy(
//                userId = userId,
//                categoriaRemoteId = categoriaRemoteId,
//                needsSync = true,
//                isSynced = false
//            )

//            val id = subcategoriaDao.insertarSubcategoria(entity)
//
//            if (userId != null && categoriaRemoteId != null) {
//                try {
//                    sincronizarSubcategoriaIndividual(id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar: ${e.message}")
//                }
//            }

            PADomainState.Success(0)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarSubcategoria(subcategoria: Subcategoria): PADomainState<Unit> {
        return try {
            val entityExistente = subcategoriaDao.obtenerSubcategoriaPorId(subcategoria.id)

//            val entity = subcategoria.toEntity().copy(
//                remoteId = entityExistente?.remoteId,
//                userId = entityExistente?.userId,
//                categoriaRemoteId = entityExistente?.categoriaRemoteId,
//                updatedAt = System.currentTimeMillis(),
//                needsSync = true,
//                isSynced = false
//            )

//            subcategoriaDao.actualizarSubcategoria(entity)
//
//            if (entity.remoteId != null) {
//                try {
//                    sincronizarSubcategoriaIndividual(subcategoria.id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar: ${e.message}")
//                }
//            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarSubcategoria(id: Long): PADomainState<Unit> {
        return try {
//            subcategoriaDao.marcarComoEliminada(id)
//
//            try {
//                val subcategoria = subcategoriaDao.obtenerSubcategoriaPorId(id)
//                if (subcategoria?.remoteId != null) {
//                    eliminarEnSupabase(subcategoria.remoteId)
//                    subcategoriaDao.eliminarPermanentemente(id)
//                }
//            } catch (e: Exception) {
//                Log.w(TAG, "No se pudo eliminar en Supabase: ${e.message}")
//            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
//
//    // Métodos de sincronización privados (similar a Categoria)
//    private suspend fun sincronizarSubcategoriaIndividual(localId: Long) {
//        val entity = subcategoriaDao.obtenerSubcategoriaPorId(localId) ?: return
//        val userId = SyncHelper.requireUserId(entity.userId)
//
//        if (entity.remoteId == null) {
//            crearEnSupabase(entity, userId)
//        } else {
//            actualizarEnSupabase(entity)
//        }
//    }
//
//    private suspend fun crearEnSupabase(entity: com.passioagogo.market.data.local.entity.base.SubcategoriaEntity, userId: String) {
//        if (entity.categoriaRemoteId == null) {
//            throw IllegalStateException("No se puede crear sin categoriaRemoteId")
//        }
//
//        SyncHelper.withRetry {
//            val createDto = entity.copy(userId = userId).toCreateDto()
//
//            val createdDto = postgrest.from("subcategorias")
//                .insert(createDto) {
//                    select()
//                }
//                .decodeSingle<SubcategoriaDto>()
//
//            subcategoriaDao.actualizarRemoteId(entity.id, createdDto.id)
//        }
//    }
//
//    private suspend fun actualizarEnSupabase(entity: com.passioagogo.market.data.local.entity.base.SubcategoriaEntity) {
//        if (entity.remoteId == null) return
//
//        SyncHelper.withRetry {
//            postgrest.from("subcategorias")
//                .update({
//                    set("nombre", entity.nombre)
//                    set("descripcion", entity.descripcion)
//                    set("activo", entity.activo)
//                }) {
//                    filter {
//                        eq("id", entity.remoteId)
//                    }
//                }
//
//            subcategoriaDao.marcarComoSincronizada(entity.id)
//        }
//    }

    private suspend fun eliminarEnSupabase(remoteId: String) {
        SyncHelper.withRetry {
            postgrest.from("subcategorias")
                .delete {
                    filter {
                        eq("id", remoteId)
                    }
                }
        }
    }
}