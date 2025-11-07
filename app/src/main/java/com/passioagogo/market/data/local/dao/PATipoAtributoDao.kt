package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.dinamics.TipoAtributoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoAtributoDao {
    // Métodos originales
    @Query("SELECT * FROM tipos_atributos WHERE activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerTiposAtributosActivos(): Flow<List<TipoAtributoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTipoAtributo(tipoAtributo: TipoAtributoEntity): Long

    @Update
    suspend fun actualizarTipoAtributo(tipoAtributo: TipoAtributoEntity)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM tipos_atributos WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerTipoAtributoPorId(id: Long): TipoAtributoEntity?

    @Query("SELECT * FROM tipos_atributos WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerTipoAtributoPorRemoteId(remoteId: String): TipoAtributoEntity?

    @Query("SELECT * FROM tipos_atributos WHERE isDeleted = 0")
    suspend fun obtenerTodosLosTiposAtributos(): List<TipoAtributoEntity>

    @Query("SELECT * FROM tipos_atributos WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getTiposAtributosPendientesSync(): List<TipoAtributoEntity>

    @Query("SELECT * FROM tipos_atributos WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getTiposAtributosNoSincronizados(): List<TipoAtributoEntity>

    @Query("SELECT * FROM tipos_atributos WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getTiposAtributosEliminadosPendientes(): List<TipoAtributoEntity>

    @Query("UPDATE tipos_atributos SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminado(id: Long)

    @Query("DELETE FROM tipos_atributos WHERE id = :id")
    suspend fun eliminarPermanentemente(id: Long)

    @Query("DELETE FROM tipos_atributos WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarTiposAtributosEliminadosSincronizados()

    @Query("UPDATE tipos_atributos SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE tipos_atributos SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("UPDATE tipos_atributos SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}