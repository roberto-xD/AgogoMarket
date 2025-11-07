package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDao {
    // Métodos originales
    @Query("SELECT * FROM proveedores WHERE activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerProveedoresActivos(): Flow<List<ProveedorEntity>>

    @Query("SELECT * FROM proveedores WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerProveedorPorId(id: Long): ProveedorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProveedor(proveedor: ProveedorEntity): Long

    @Update
    suspend fun actualizarProveedor(proveedor: ProveedorEntity)

    @Query("UPDATE proveedores SET activo = 0 WHERE id = :id")
    suspend fun eliminarProveedor(id: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM proveedores WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerProveedorPorRemoteId(remoteId: String): ProveedorEntity?

    @Query("SELECT * FROM proveedores WHERE isDeleted = 0")
    suspend fun obtenerTodosLosProveedores(): List<ProveedorEntity>

    @Query("SELECT * FROM proveedores WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getProveedoresPendientesSync(): List<ProveedorEntity>

    @Query("SELECT * FROM proveedores WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getProveedoresNoSincronizados(): List<ProveedorEntity>

    @Query("SELECT * FROM proveedores WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getProveedoresEliminadosPendientes(): List<ProveedorEntity>

    @Query("UPDATE proveedores SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminado(id: Long)

    @Query("DELETE FROM proveedores WHERE id = :id")
    suspend fun eliminarPermanentemente(id: Long)

    @Query("DELETE FROM proveedores WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarProveedoresEliminadosSincronizados()

    @Query("UPDATE proveedores SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE proveedores SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("UPDATE proveedores SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}