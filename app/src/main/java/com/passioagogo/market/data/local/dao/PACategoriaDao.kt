package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    // Métodos originales
    @Query("SELECT * FROM categorias WHERE activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerCategoriasActivas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE familiaId = :familiaId AND activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerCategoriasPorFamiliaId(familiaId: Long): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerCategoriaPorId(id: Long): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategoria(categoria: CategoriaEntity): Long

    @Update
    suspend fun actualizarCategoria(categoria: CategoriaEntity)

    @Query("UPDATE categorias SET activo = 0 WHERE id = :id")
    suspend fun eliminarCategoria(id: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM categorias WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerCategoriaPorRemoteId(remoteId: String): CategoriaEntity?

    @Query("SELECT * FROM categorias WHERE isDeleted = 0")
    suspend fun obtenerTodasLasCategorias(): List<CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getCategoriasPendientesSync(): List<CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getCategoriasNoSincronizadas(): List<CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getCategoriasEliminadasPendientes(): List<CategoriaEntity>

    // Buscar por familiaRemoteId (útil al sincronizar)
    @Query("SELECT * FROM categorias WHERE familiaRemoteId = :familiaRemoteId AND isDeleted = 0")
    suspend fun obtenerCategoriasPorFamiliaRemoteId(familiaRemoteId: String): List<CategoriaEntity>

    @Query("UPDATE categorias SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminada(id: Long)

    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun eliminarPermanentemente(id: Long)

    @Query("DELETE FROM categorias WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarCategoriasEliminadasSincronizadas()

    @Query("UPDATE categorias SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE categorias SET familiaRemoteId = :familiaRemoteId WHERE id = :localId")
    suspend fun actualizarFamiliaRemoteId(localId: Long, familiaRemoteId: String)

    @Query("UPDATE categorias SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizada(id: Long)

    @Query("UPDATE categorias SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}