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
}