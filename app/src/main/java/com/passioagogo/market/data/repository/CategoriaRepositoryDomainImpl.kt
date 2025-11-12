package com.passioagogo.market.data.repository

import android.util.Log
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
//import com.passioagogo.market.data.mapper.toCreateDto
//import com.passioagogo.market.data.mapper.toEntity
import com.passioagogo.market.data.remote.dto.CategoriaDto
import com.passioagogo.market.data.repository.sync.ConflictResolver
import com.passioagogo.market.data.repository.sync.ConflictStrategy
import com.passioagogo.market.data.repository.sync.SyncHelper
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
    private val familiaDao: FamiliaDao,
    private val auth: Auth,
    private val postgrest: Postgrest
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
            val userId = auth.currentUserOrNull()?.id

            // Obtener familiaRemoteId
            val familia = familiaDao.obtenerFamiliaPorId(categoria.familiaId)
//            val familiaRemoteId = familia?.remoteId

//            val entity = categoria.toEntity().copy(
//                userId = userId,
//                familiaRemoteId = familiaRemoteId,
//                needsSync = true,
//                isSynced = false
//            )
//
//            val id = categoriaDao.insertarCategoria(entity)

//            // Sincronizar si la familia ya está sincronizada
//            if (userId != null && familiaRemoteId != null) {
//                try {
//                    sincronizarCategoriaIndividual(id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar: ${e.message}")
//                }
//            }

            PADomainState.Success(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar categoría: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarCategoria(categoria: Categoria): PADomainState<Unit> {
        return try {
            val entityExistente = categoriaDao.obtenerCategoriaPorId(categoria.id)

//            val entity = categoria.toEntity().copy(
//                remoteId = entityExistente?.remoteId,
//                userId = entityExistente?.userId,
//                familiaRemoteId = entityExistente?.familiaRemoteId,
//                updatedAt = System.currentTimeMillis(),
//                needsSync = true,
//                isSynced = false
//            )

//            categoriaDao.actualizarCategoria(entity)

//            if (entity.remoteId != null) {
//                try {
//                    sincronizarCategoriaIndividual(categoria.id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar: ${e.message}")
//                }
//            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar categoría: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarCategoria(id: Long): PADomainState<Unit> {
        return try {
//            categoriaDao.marcarComoEliminada(id)

            try {
                val categoria = categoriaDao.obtenerCategoriaPorId(id)
//                if (categoria?.remoteId != null) {
//                    eliminarEnSupabase(categoria.remoteId)
//                    categoriaDao.eliminarPermanentemente(id)
//                }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo eliminar en Supabase: ${e.message}")
            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar categoría: ${e.message}")
            PADomainState.Error(e)
        }
    }
//
//    // ========================================
//    // SINCRONIZACIÓN
//    // ========================================
//
//    override suspend fun sincronizarConSupabase(): PADomainState<Unit> {
//        return try {
//            val userId = auth.currentUserOrNull()?.id
//                ?: return PADomainState.Error(Exception("Usuario no autenticado"))
//
//            Log.d(TAG, "Iniciando sincronización...")
//
//            sincronizarCambiosLocales(userId)
//            sincronizarCambiosRemotos(userId)
//            categoriaDao.limpiarCategoriasEliminadasSincronizadas()
//
//            PADomainState.Success(Unit)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error en sincronización: ${e.message}")
//            PADomainState.Error(e)
//        }
//    }
//
//    // ========================================
//    // MÉTODOS PRIVADOS
//    // ========================================
//
//    private suspend fun sincronizarCambiosLocales(userId: String) {
//        Log.d(TAG, "Sincronizando cambios locales...")
//
//        // Nuevas
//        val categoriasNuevas = categoriaDao.getCategoriasNoSincronizadas()
//            .filter { it.familiaRemoteId != null }
//
//        categoriasNuevas.forEach { entity ->
//            try {
//                crearEnSupabase(entity, userId)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error al crear categoría ${entity.id}: ${e.message}")
//            }
//        }
//
//        // Modificadas
//        val categoriasModificadas = categoriaDao.getCategoriasPendientesSync()
//            .filter { it.remoteId != null }
//
//        categoriasModificadas.forEach { entity ->
//            try {
//                actualizarEnSupabase(entity)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error al actualizar: ${e.message}")
//            }
//        }
//
//        // Eliminadas
//        val categoriasEliminadas = categoriaDao.getCategoriasEliminadasPendientes()
//        categoriasEliminadas.forEach { entity ->
//            try {
//                if (entity.remoteId != null) {
//                    eliminarEnSupabase(entity.remoteId)
//                }
//                categoriaDao.eliminarPermanentemente(entity.id)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error al eliminar: ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun sincronizarCambiosRemotos(userId: String) {
//        Log.d(TAG, "Sincronizando cambios remotos...")
//
//        try {
//            val categoriasDtos = postgrest.from("categorias")
//                .select {
//                    filter {
//                        eq("user_id", userId)
//                    }
//                }
//                .decodeList<CategoriaDto>()
//
//            categoriasDtos.forEach { dto ->
//                try {
//                    val familiaLocal = familiaDao.obtenerFamiliaPorRemoteId(dto.familiaId)
//
//                    if (familiaLocal != null) {
//                        val localCategoria = categoriaDao.obtenerCategoriaPorRemoteId(dto.id)
//
//                        if (localCategoria != null) {
//                            if (localCategoria.needsSync) {
//                                val resolved = ConflictResolver.resolveConflict(
//                                    local = localCategoria,
//                                    remote = dto.toEntity(
//                                        localId = localCategoria.id,
//                                        localFamiliaId = familiaLocal.id
//                                    ),
//                                    strategy = ConflictStrategy.NEWEST_WINS,
//                                    localTimestamp = localCategoria.updatedAt,
//                                    remoteTimestamp = com.passioagogo.market.data.mapper.DateMapper.parseIsoDate(dto.updatedAt)
//                                )
//                                categoriaDao.actualizarCategoria(resolved)
//                            } else {
//                                categoriaDao.actualizarCategoria(
//                                    dto.toEntity(
//                                        localId = localCategoria.id,
//                                        localFamiliaId = familiaLocal.id
//                                    )
//                                )
//                            }
//                        } else {
//                            categoriaDao.insertarCategoria(
//                                dto.toEntity(localFamiliaId = familiaLocal.id)
//                            )
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error al procesar: ${e.message}")
//                }
//            }
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//
//    private suspend fun sincronizarCategoriaIndividual(localId: Long) {
//        val entity = categoriaDao.obtenerCategoriaPorId(localId) ?: return
//        val userId = SyncHelper.requireUserId(entity.userId)
//
//        if (entity.remoteId == null) {
//            crearEnSupabase(entity, userId)
//        } else {
//            actualizarEnSupabase(entity)
//        }
//    }
//
//    private suspend fun crearEnSupabase(entity: com.passioagogo.market.data.local.entity.base.CategoriaEntity, userId: String) {
//        if (entity.familiaRemoteId == null) {
//            throw IllegalStateException("No se puede crear sin familiaRemoteId")
//        }
//
//        SyncHelper.withRetry {
//            val createDto = entity.copy(userId = userId).toCreateDto()
//
//            val createdDto = postgrest.from("categorias")
//                .insert(createDto) {
//                    select()
//                }
//                .decodeSingle<CategoriaDto>()
//
//            categoriaDao.actualizarRemoteId(entity.id, createdDto.id)
//        }
//    }
//
//    private suspend fun actualizarEnSupabase(entity: com.passioagogo.market.data.local.entity.base.CategoriaEntity) {
//        if (entity.remoteId == null) return
//
//        SyncHelper.withRetry {
//            postgrest.from("categorias")
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
//            categoriaDao.marcarComoSincronizada(entity.id)
//        }
//    }

    private suspend fun eliminarEnSupabase(remoteId: String) {
        SyncHelper.withRetry {
            postgrest.from("categorias")
                .delete {
                    filter {
                        eq("id", remoteId)
                    }
                }
        }
    }
}