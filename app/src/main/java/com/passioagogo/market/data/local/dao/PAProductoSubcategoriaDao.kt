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
}