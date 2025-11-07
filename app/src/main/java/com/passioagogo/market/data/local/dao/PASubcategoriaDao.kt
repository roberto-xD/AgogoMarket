package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoriaDao {
    // Métodos originales
    @Query("SELECT * FROM subcategorias WHERE activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerSubcategoriasActivas(): Flow<List<SubcategoriaEntity>>

    @Query("SELECT * FROM subcategorias WHERE categoriaId = :categoriaId AND activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerSubcategoriasPorCategoria(categoriaId: Long): Flow<List<SubcategoriaEntity>>

    @Query("SELECT * FROM subcategorias WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerSubcategoriaPorId(id: Long): SubcategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSubcategoria(subcategoria: SubcategoriaEntity): Long

    @Update
    suspend fun actualizarSubcategoria(subcategoria: SubcategoriaEntity)

    @Query("UPDATE subcategorias SET activo = 0 WHERE id = :id")
    suspend fun eliminarSubcategoria(id: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM subcategorias WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerSubcategoriaPorRemoteId(remoteId: String): SubcategoriaEntity?

    @Query("SELECT * FROM subcategorias WHERE isDeleted = 0")
    suspend fun obtenerTodasLasSubcategorias(): List<SubcategoriaEntity>

    @Query("SELECT * FROM subcategorias WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getSubcategoriasPendientesSync(): List<SubcategoriaEntity>

    @Query("SELECT * FROM subcategorias WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getSubcategoriasNoSincronizadas(): List<SubcategoriaEntity>

    @Query("SELECT * FROM subcategorias WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getSubcategoriasEliminadasPendientes(): List<SubcategoriaEntity>

    @Query("SELECT * FROM subcategorias WHERE categoriaRemoteId = :categoriaRemoteId AND isDeleted = 0")
    suspend fun obtenerSubcategoriasPorCategoriaRemoteId(categoriaRemoteId: String): List<SubcategoriaEntity>

    @Query("UPDATE subcategorias SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminada(id: Long)

    @Query("DELETE FROM subcategorias WHERE id = :id")
    suspend fun eliminarPermanentemente(id: Long)

    @Query("DELETE FROM subcategorias WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarSubcategoriasEliminadasSincronizadas()

    @Query("UPDATE subcategorias SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE subcategorias SET categoriaRemoteId = :categoriaRemoteId WHERE id = :localId")
    suspend fun actualizarCategoriaRemoteId(localId: Long, categoriaRemoteId: String)

    @Query("UPDATE subcategorias SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizada(id: Long)

    @Query("UPDATE subcategorias SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}