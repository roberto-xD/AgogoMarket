package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passioagogo.market.data.local.entity.relation.ProductoFamiliaEntity

@Dao
interface ProductoFamiliaDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoFamilia(productoFamilia: ProductoFamiliaEntity)

    @Query("DELETE FROM producto_familia WHERE productoId = :productoId")
    suspend fun eliminarFamiliaDeProducto(productoId: Long)

    @Query("SELECT * FROM producto_familia WHERE productoId = :productoId")
    suspend fun obtenerFamiliaPorProducto(productoId: Long): ProductoFamiliaEntity?

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM producto_familia WHERE productoId = :productoId AND familiaId = :familiaId")
    suspend fun obtenerRelacion(productoId: Long, familiaId: Long): ProductoFamiliaEntity?

    @Query("SELECT * FROM producto_familia")
    suspend fun obtenerTodasLasRelaciones(): List<ProductoFamiliaEntity>

    @Query("SELECT * FROM producto_familia WHERE needsSync = 1")
    suspend fun getRelacionesPendientesSync(): List<ProductoFamiliaEntity>

    @Query("UPDATE producto_familia SET productoRemoteId = :productoRemoteId, familiaRemoteId = :familiaRemoteId, isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND familiaId = :familiaId")
    suspend fun actualizarRemoteIds(productoId: Long, familiaId: Long, productoRemoteId: String, familiaRemoteId: String)

    @Query("UPDATE producto_familia SET isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND familiaId = :familiaId")
    suspend fun marcarComoSincronizada(productoId: Long, familiaId: Long)

    @Query("UPDATE producto_familia SET needsSync = 1, isSynced = 0 WHERE productoId = :productoId")
    suspend fun marcarRelacionesDeProductoComoPendientes(productoId: Long)
}