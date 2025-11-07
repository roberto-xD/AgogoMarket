package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoCategoriaEntity

@Dao
interface ProductoCategoriaDao {
    @Query("""
        SELECT c.* FROM categorias c
        INNER JOIN producto_categorias pc ON c.id = pc.categoriaId
        WHERE pc.productoId = :productoId AND c.activo = 1
    """)
    suspend fun obtenerCategoriasPorProducto(productoId: Long): List<CategoriaEntity>

    @Query("""
        SELECT c.* FROM categorias c
        INNER JOIN producto_categorias pc ON c.id = pc.categoriaId
        WHERE pc.productoId = :productoId AND pc.esPrincipal = 1 AND c.activo = 1
        LIMIT 1
    """)
    suspend fun obtenerCategoriaPrincipalPorProducto(productoId: Long): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoCategoria(productoCategoria: ProductoCategoriaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoCategorias(productosCategorias: List<ProductoCategoriaEntity>)

    @Query("DELETE FROM producto_categorias WHERE productoId = :productoId")
    suspend fun eliminarCategoriasDeProducto(productoId: Long)

    @Query("DELETE FROM producto_categorias WHERE productoId = :productoId AND categoriaId = :categoriaId")
    suspend fun eliminarProductoCategoria(productoId: Long, categoriaId: Long)

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM producto_categorias WHERE productoId = :productoId AND categoriaId = :categoriaId")
    suspend fun obtenerRelacion(productoId: Long, categoriaId: Long): ProductoCategoriaEntity?

    @Query("SELECT * FROM producto_categorias WHERE productoId = :productoId")
    suspend fun obtenerRelacionesPorProducto(productoId: Long): List<ProductoCategoriaEntity>

    @Query("SELECT * FROM producto_categorias")
    suspend fun obtenerTodasLasRelaciones(): List<ProductoCategoriaEntity>

    @Query("SELECT * FROM producto_categorias WHERE needsSync = 1")
    suspend fun getRelacionesPendientesSync(): List<ProductoCategoriaEntity>

    @Query("UPDATE producto_categorias SET productoRemoteId = :productoRemoteId, categoriaRemoteId = :categoriaRemoteId, isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND categoriaId = :categoriaId")
    suspend fun actualizarRemoteIds(productoId: Long, categoriaId: Long, productoRemoteId: String, categoriaRemoteId: String)

    @Query("UPDATE producto_categorias SET isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND categoriaId = :categoriaId")
    suspend fun marcarComoSincronizada(productoId: Long, categoriaId: Long)

    @Query("UPDATE producto_categorias SET needsSync = 1, isSynced = 0 WHERE productoId = :productoId")
    suspend fun marcarRelacionesDeProductoComoPendientes(productoId: Long)
}