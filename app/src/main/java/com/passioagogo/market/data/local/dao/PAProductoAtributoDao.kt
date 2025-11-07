package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.dinamics.ProductoAtributoEntity
import com.passioagogo.market.data.local.entity.utils.ProductoAtributoConTipo

@Dao
interface ProductoAtributoDao {
    @Query("""
        SELECT pa.*, ta.nombre as nombreAtributo, ta.tipoDato FROM producto_atributos pa
        INNER JOIN tipos_atributos ta ON pa.tipoAtributoId = ta.id
        WHERE pa.productoId = :productoId AND ta.activo = 1 AND pa.isDeleted = 0
        ORDER BY ta.nombre
    """)
    suspend fun obtenerAtributosPorProducto(productoId: Long): List<ProductoAtributoConTipo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoAtributo(productoAtributo: ProductoAtributoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoAtributos(productosAtributos: List<ProductoAtributoEntity>)

    @Query("DELETE FROM producto_atributos WHERE productoId = :productoId")
    suspend fun eliminarAtributosDeProducto(productoId: Long)

    @Update
    suspend fun actualizarProductoAtributo(productoAtributo: ProductoAtributoEntity)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM producto_atributos WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerProductoAtributoPorId(id: Long): ProductoAtributoEntity?

    @Query("SELECT * FROM producto_atributos WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerProductoAtributoPorRemoteId(remoteId: String): ProductoAtributoEntity?

    @Query("SELECT * FROM producto_atributos WHERE productoId = :productoId AND isDeleted = 0")
    suspend fun obtenerTodosAtributosPorProducto(productoId: Long): List<ProductoAtributoEntity>

    @Query("SELECT * FROM producto_atributos WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getProductoAtributosPendientesSync(): List<ProductoAtributoEntity>

    @Query("SELECT * FROM producto_atributos WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getProductoAtributosNoSincronizados(): List<ProductoAtributoEntity>

    @Query("SELECT * FROM producto_atributos WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getProductoAtributosEliminadosPendientes(): List<ProductoAtributoEntity>

    @Query("UPDATE producto_atributos SET isDeleted = 1, needsSync = 1 WHERE productoId = :productoId")
    suspend fun marcarAtributosDeProductoComoEliminados(productoId: Long)

    @Query("UPDATE producto_atributos SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE producto_atributos SET productoRemoteId = :productoRemoteId, tipoAtributoRemoteId = :tipoAtributoRemoteId WHERE id = :localId")
    suspend fun actualizarForeignRemoteIds(localId: Long, productoRemoteId: String, tipoAtributoRemoteId: String)

    @Query("UPDATE producto_atributos SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("UPDATE producto_atributos SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)
}