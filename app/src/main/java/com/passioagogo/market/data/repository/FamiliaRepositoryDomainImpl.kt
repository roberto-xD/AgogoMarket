package com.passioagogo.market.data.repository

import android.util.Log
import com.passioagogo.market.data.local.dao.FamiliaDao
//import com.passioagogo.market.data.mapper.toCreateDto
//import com.passioagogo.market.data.mapper.toEntity
import com.passioagogo.market.data.remote.dto.FamiliaDto
import com.passioagogo.market.data.repository.sync.ConflictResolver
import com.passioagogo.market.data.repository.sync.ConflictStrategy
import com.passioagogo.market.data.repository.sync.SyncHelper
import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.repository.FamiliaRepository
import com.passioagogo.market.domain.state.PADomainState
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class FamiliaRepositoryDomainImpl @Inject constructor(
    private val familiaDao: FamiliaDao,
    private val auth: Auth,
    private val postgrest: Postgrest
) : FamiliaRepository {

    companion object {
        private const val TAG = "FamiliaRepository"
    }

    // ========================================
    // OPERACIONES LOCALES (MANTIENEN TU LÓGICA ORIGINAL)
    // ========================================

    override suspend fun obtenerTodasLasFamilias(): Flow<List<Familia>> {
        return familiaDao.obtenerFamiliasActivas().map { entities ->
            entities.map { Familia.fromEntity(it) }
        }
    }

    override suspend fun obtenerFamiliaPorId(id: Long): Familia? {
        return familiaDao.obtenerFamiliaPorId(id)?.let { Familia.fromEntity(it) }
    }

    override suspend fun guardarFamilia(familia: Familia): PADomainState<Long> {
        return try {
            val userId = auth.currentUserOrNull()?.id

            // Convertir a Entity y agregar campos de sincronización
//            val entity = familia.toEntity().copy(
//                userId = userId,
//                needsSync = true,
//                isSynced = false
//            )

//            val id = familiaDao.insertarFamilia(entity)

            // Intentar sincronizar inmediatamente en background
//            if (userId != null) {
//                try {
//                    sincronizarFamiliaIndividual(id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar inmediatamente: ${e.message}")
//                }
//            }

            PADomainState.Success(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar familia: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarFamilia(familia: Familia): PADomainState<Unit> {
        return try {
            // Obtener entity existente para preservar campos de sincronización
            val entityExistente = familiaDao.obtenerFamiliaPorId(familia.id)

//            val entity = familia.toEntity().copy(
//                remoteId = entityExistente?.remoteId,
//                userId = entityExistente?.userId,
//                updatedAt = System.currentTimeMillis(),
//                needsSync = true,
//                isSynced = false
//            )

//            familiaDao.actualizarFamilia(entity)

            // Intentar sincronizar si tiene remoteId
//            if (entity.remoteId != null) {
//                try {
//                    sincronizarFamiliaIndividual(familia.id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar inmediatamente: ${e.message}")
//                }
//            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar familia: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarFamilia(id: Long): PADomainState<Unit> {
        return try {
            // Soft delete
//            familiaDao.marcarComoEliminada(id)

            // Intentar eliminar en Supabase
//            try {
//                val familia = familiaDao.obtenerFamiliaPorId(id)
//                if (familia?.remoteId != null) {
//                    eliminarEnSupabase(familia.remoteId)
//                    familiaDao.eliminarPermanentemente(id)
//                }
//            } catch (e: Exception) {
//                Log.w(TAG, "No se pudo eliminar en Supabase: ${e.message}")
//            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar familia: ${e.message}")
            PADomainState.Error(e)
        }
    }

    // ========================================
    // SINCRONIZACIÓN (NUEVOS MÉTODOS)
    // ========================================
//
//    override suspend fun sincronizarConSupabase(): PADomainState<Unit> {
//        return try {
//            val userId = auth.currentUserOrNull()?.id
//            if (userId == null) {
//                return PADomainState.Error(Exception("Usuario no autenticado"))
//            }
//
//            Log.d(TAG, "Iniciando sincronización completa...")
//
//            // 1. Sincronizar cambios locales → Supabase
//            sincronizarCambiosLocales(userId)
//
//            // 2. Obtener cambios remotos → Local
//            sincronizarCambiosRemotos(userId)
//
//            // 3. Limpiar eliminadas sincronizadas
//            familiaDao.limpiarFamiliasEliminadasSincronizadas()
//
//            Log.d(TAG, "Sincronización completa exitosa")
//            PADomainState.Success(Unit)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error en sincronización: ${e.message}")
//            PADomainState.Error(e)
//        }
//    }
//
//    override suspend fun forzarSincronizacion(): PADomainState<Unit> {
//        Log.d(TAG, "Forzando sincronización completa...")
//        return sincronizarConSupabase()
//    }
//
//    // ========================================
//    // MÉTODOS PRIVADOS DE SINCRONIZACIÓN
//    // ========================================
//
//    private suspend fun sincronizarCambiosLocales(userId: String) {
//        Log.d(TAG, "Sincronizando cambios locales...")
//
//        // 1. Familias nuevas (sin remoteId)
//        val familiasNuevas = familiaDao.getFamiliasNoSincronizadas()
//        Log.d(TAG, "Familias nuevas a sincronizar: ${familiasNuevas.size}")
//
//        familiasNuevas.forEach { entity ->
//            try {
//                crearEnSupabase(entity, userId)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error al crear familia ${entity.id} en Supabase: ${e.message}")
//            }
//        }
//
//        // 2. Familias modificadas
//        val familiasModificadas = familiaDao.getFamiliasPendientesSync()
//            .filter { it.remoteId != null }
//        Log.d(TAG, "Familias modificadas a sincronizar: ${familiasModificadas.size}")
//
//        familiasModificadas.forEach { entity ->
//            try {
//                actualizarEnSupabase(entity)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error al actualizar familia ${entity.id}: ${e.message}")
//            }
//        }
//
//        // 3. Familias eliminadas
//        val familiasEliminadas = familiaDao.getFamiliasEliminadasPendientes()
//        Log.d(TAG, "Familias eliminadas a sincronizar: ${familiasEliminadas.size}")
//
//        familiasEliminadas.forEach { entity ->
//            try {
//                if (entity.remoteId != null) {
//                    eliminarEnSupabase(entity.remoteId)
//                }
//                familiaDao.eliminarPermanentemente(entity.id)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error al eliminar familia ${entity.id}: ${e.message}")
//            }
//        }
//    }
//
//    private suspend fun sincronizarCambiosRemotos(userId: String) {
//        Log.d(TAG, "Sincronizando cambios remotos...")
//
//        try {
//            val familiasDtos = postgrest.from("familias")
//                .select {
//                    filter {
//                        eq("user_id", userId)
//                    }
//                }
//                .decodeList<FamiliaDto>()
//
//            Log.d(TAG, "Familias obtenidas de Supabase: ${familiasDtos.size}")
//
//            familiasDtos.forEach { dto ->
//                try {
//                    val localFamilia = familiaDao.obtenerFamiliaPorRemoteId(dto.id)
//
//                    if (localFamilia != null) {
//                        // Ya existe localmente
//                        if (localFamilia.needsSync) {
//                            // Resolver conflicto
//                            val resolved = ConflictResolver.resolveConflict(
//                                local = localFamilia,
//                                remote = dto.toEntity(localId = localFamilia.id),
//                                strategy = ConflictStrategy.NEWEST_WINS,
//                                localTimestamp = localFamilia.updatedAt,
//                                remoteTimestamp = com.passioagogo.market.data.mapper.DateMapper.parseIsoDate(dto.updatedAt)
//                            )
//                            familiaDao.actualizarFamilia(resolved)
//                        } else {
//                            // Actualizar con datos remotos
//                            familiaDao.actualizarFamilia(
//                                dto.toEntity(localId = localFamilia.id)
//                            )
//                        }
//                    } else {
//                        // No existe, insertar
//                        familiaDao.insertarFamilia(dto.toEntity())
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error al procesar familia remota ${dto.id}: ${e.message}")
//                }
//            }
//
//            // Detectar eliminadas remotamente
//            detectarEliminadasRemotas(familiasDtos, userId)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error al obtener familias remotas: ${e.message}")
//            throw e
//        }
//    }
//
//    private suspend fun detectarEliminadasRemotas(
//        familiasDtos: List<FamiliaDto>,
//        userId: String
//    ) {
//        val remoteIds = familiasDtos.map { it.id }.toSet()
//        val localFamilias = familiaDao.obtenerTodasLasFamilias()
//            .filter { it.userId == userId && it.remoteId != null }
//
//        localFamilias.forEach { local ->
//            if (local.remoteId !in remoteIds && local.isSynced) {
//                Log.d(TAG, "Familia ${local.id} eliminada remotamente")
//                familiaDao.eliminarPermanentemente(local.id)
//            }
//        }
//    }
//
//    private suspend fun sincronizarFamiliaIndividual(localId: Long) {
//        val entity = familiaDao.obtenerFamiliaPorId(localId) ?: return
//        val userId = SyncHelper.requireUserId(entity.userId)
//
//        if (entity.remoteId == null) {
//            crearEnSupabase(entity, userId)
//        } else {
//            actualizarEnSupabase(entity)
//        }
//    }
//
//    private suspend fun crearEnSupabase(entity: com.passioagogo.market.data.local.entity.base.FamiliaEntity, userId: String) {
//        SyncHelper.withRetry {
//            Log.d(TAG, "Creando familia ${entity.id} en Supabase...")
//
//            val createDto = entity.copy(userId = userId).toCreateDto()
//
//            val createdDto = postgrest.from("familias")
//                .insert(createDto) {
//                    select()
//                }
//                .decodeSingle<FamiliaDto>()
//
//            familiaDao.actualizarRemoteId(entity.id, createdDto.id)
//
//            Log.d(TAG, "Familia creada con remoteId: ${createdDto.id}")
//        }
//    }
//
//    private suspend fun actualizarEnSupabase(entity: com.passioagogo.market.data.local.entity.base.FamiliaEntity) {
//        if (entity.remoteId == null) return
//
//        SyncHelper.withRetry {
//            Log.d(TAG, "Actualizando familia ${entity.id}...")
//
//            postgrest.from("familias")
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
//            familiaDao.marcarComoSincronizada(entity.id)
//        }
//    }

    private suspend fun eliminarEnSupabase(remoteId: String) {
        SyncHelper.withRetry {
            postgrest.from("familias")
                .delete {
                    filter {
                        eq("id", remoteId)
                    }
                }
        }
    }
}