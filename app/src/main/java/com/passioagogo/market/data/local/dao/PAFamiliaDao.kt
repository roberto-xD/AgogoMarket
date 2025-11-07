package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamiliaDao {
    // Métodos originales
    @Query("SELECT * FROM familias WHERE activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerFamiliasActivas(): Flow<List<FamiliaEntity>>

    @Query("SELECT * FROM familias WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerFamiliaPorId(id: Long): FamiliaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFamilia(familia: FamiliaEntity): Long

    @Update
    suspend fun actualizarFamilia(familia: FamiliaEntity)

    @Query("UPDATE familias SET activo = 0 WHERE id = :id")
    suspend fun eliminarFamilia(id: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    // Buscar por remoteId (para sincronización)
    @Query("SELECT * FROM familias WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerFamiliaPorRemoteId(remoteId: String): FamiliaEntity?

    // Obtener todas las familias (incluyendo inactivas, para sincronización completa)
    @Query("SELECT * FROM familias WHERE isDeleted = 0")
    suspend fun obtenerTodasLasFamilias(): List<FamiliaEntity>

    // Obtener familias pendientes de sincronización
    @Query("SELECT * FROM familias WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getFamiliasPendientesSync(): List<FamiliaEntity>

    // Obtener familias no sincronizadas (sin remoteId)
    @Query("SELECT * FROM familias WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getFamiliasNoSincronizadas(): List<FamiliaEntity>

    // Obtener familias eliminadas pendientes de sincronizar
    @Query("SELECT * FROM familias WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getFamiliasEliminadasPendientes(): List<FamiliaEntity>

    // Marcar como eliminada (soft delete)
    @Query("UPDATE familias SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminada(id: Long)

    // Eliminar permanentemente (después de sincronizar)
    @Query("DELETE FROM familias WHERE id = :id")
    suspend fun eliminarPermanentemente(id: Long)

    // Limpiar familias eliminadas y sincronizadas
    @Query("DELETE FROM familias WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarFamiliasEliminadasSincronizadas()

    // Actualizar remoteId después de sincronizar
    @Query("UPDATE familias SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    // Marcar como sincronizada
    @Query("UPDATE familias SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizada(id: Long)

    // Marcar como pendiente de sincronización
    @Query("UPDATE familias SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}