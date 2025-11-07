package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passioagogo.market.data.local.entity.utils.HistorialPrecioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialPrecioDao {
    // Métodos originales
    @Query("SELECT * FROM historial_precios WHERE productoId = :productoId AND isDeleted = 0 ORDER BY fechaCambio DESC")
    fun obtenerHistorialPorProducto(productoId: Long): Flow<List<HistorialPrecioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHistorialPrecio(historial: HistorialPrecioEntity): Long

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM historial_precios WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerHistorialPorId(id: Long): HistorialPrecioEntity?

    @Query("SELECT * FROM historial_precios WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerHistorialPorRemoteId(remoteId: String): HistorialPrecioEntity?

    @Query("SELECT * FROM historial_precios WHERE isDeleted = 0")
    suspend fun obtenerTodoElHistorial(): List<HistorialPrecioEntity>

    @Query("SELECT * FROM historial_precios WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getHistorialPendienteSync(): List<HistorialPrecioEntity>

    @Query("SELECT * FROM historial_precios WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getHistorialNoSincronizado(): List<HistorialPrecioEntity>

    @Query("UPDATE historial_precios SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE historial_precios SET productoRemoteId = :productoRemoteId WHERE id = :localId")
    suspend fun actualizarProductoRemoteId(localId: Long, productoRemoteId: String)

    @Query("UPDATE historial_precios SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)
}