package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoSubcategoriaEntity

@Dao
interface ProductoSubcategoriaDao {
    @Query("""
        SELECT s.* FROM subcategorias s
        INNER JOIN producto_subcategorias ps ON s.id = ps.subcategoriaId
        WHERE ps.productoId = :productoId AND s.activo = 1
    """)
    suspend fun obtenerSubcategoriasPorProducto(productoId: Long): List<SubcategoriaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoSubcategoria(productoSubcategoria: ProductoSubcategoriaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoSubcategorias(productosSubcategorias: List<ProductoSubcategoriaEntity>)

    @Query("DELETE FROM producto_subcategorias WHERE productoId = :productoId")
    suspend fun eliminarSubcategoriasDeProducto(productoId: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM producto_subcategorias WHERE productoId = :productoId AND subcategoriaId = :subcategoriaId")
    suspend fun obtenerRelacion(productoId: Long, subcategoriaId: Long): ProductoSubcategoriaEntity?

    @Query("SELECT * FROM producto_subcategorias WHERE productoId = :productoId")
    suspend fun obtenerRelacionesPorProducto(productoId: Long): List<ProductoSubcategoriaEntity>

    @Query("SELECT * FROM producto_subcategorias")
    suspend fun obtenerTodasLasRelaciones(): List<ProductoSubcategoriaEntity>

    @Query("SELECT * FROM producto_subcategorias WHERE needsSync = 1")
    suspend fun getRelacionesPendientesSync(): List<ProductoSubcategoriaEntity>

    @Query("UPDATE producto_subcategorias SET productoRemoteId = :productoRemoteId, subcategoriaRemoteId = :subcategoriaRemoteId, isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND subcategoriaId = :subcategoriaId")
    suspend fun actualizarRemoteIds(productoId: Long, subcategoriaId: Long, productoRemoteId: String, subcategoriaRemoteId: String)

    @Query("UPDATE producto_subcategorias SET isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND subcategoriaId = :subcategoriaId")
    suspend fun marcarComoSincronizada(productoId: Long, subcategoriaId: Long)

    @Query("UPDATE producto_subcategorias SET needsSync = 1, isSynced = 0 WHERE productoId = :productoId")
    suspend fun marcarRelacionesDeProductoComoPendientes(productoId: Long)
}