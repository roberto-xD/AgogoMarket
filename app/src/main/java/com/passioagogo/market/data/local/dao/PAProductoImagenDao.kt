package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity

@Dao
interface ProductoImagenDao {
    // Métodos originales
    @Query("SELECT * FROM producto_imagenes WHERE isDeleted = 0")
    suspend fun obtenerTodasLasImagenes(): List<ProductoImagenEntity>

    @Query("SELECT * FROM producto_imagenes WHERE productoId = :productoId AND isDeleted = 0 ORDER BY orden, fechaCreacion")
    suspend fun obtenerImagenesPorProducto(productoId: Long): List<ProductoImagenEntity>

    @Query("SELECT * FROM producto_imagenes WHERE productoId = :productoId AND esPrincipal = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun obtenerImagenPrincipal(productoId: Long): ProductoImagenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarImagen(imagen: ProductoImagenEntity): Long

    @Update
    suspend fun actualizarImagen(imagen: ProductoImagenEntity)

    @Query("DELETE FROM producto_imagenes WHERE id = :id")
    suspend fun eliminarImagen(id: Long)

    @Query("DELETE FROM producto_imagenes WHERE productoId = :productoId")
    suspend fun eliminarImagenesDeProducto(productoId: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM producto_imagenes WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerImagenPorId(id: Long): ProductoImagenEntity?

    @Query("SELECT * FROM producto_imagenes WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerImagenPorRemoteId(remoteId: String): ProductoImagenEntity?

    @Query("SELECT * FROM producto_imagenes WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getImagenesPendientesSync(): List<ProductoImagenEntity>

    @Query("SELECT * FROM producto_imagenes WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getImagenesNoSincronizadas(): List<ProductoImagenEntity>

    @Query("SELECT * FROM producto_imagenes WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getImagenesEliminadasPendientes(): List<ProductoImagenEntity>

    @Query("UPDATE producto_imagenes SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminada(id: Long)

    @Query("UPDATE producto_imagenes SET isDeleted = 1, needsSync = 1 WHERE productoId = :productoId")
    suspend fun marcarImagenesDeProductoComoEliminadas(productoId: Long)

    @Query("DELETE FROM producto_imagenes WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarImagenesEliminadasSincronizadas()

    @Query("UPDATE producto_imagenes SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE producto_imagenes SET productoRemoteId = :productoRemoteId WHERE id = :localId")
    suspend fun actualizarProductoRemoteId(localId: Long, productoRemoteId: String)

    @Query("UPDATE producto_imagenes SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizada(id: Long)

    @Query("UPDATE producto_imagenes SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}