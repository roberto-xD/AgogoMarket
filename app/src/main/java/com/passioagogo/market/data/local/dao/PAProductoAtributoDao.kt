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
        WHERE pa.productoId = :productoId AND ta.activo = 1
        ORDER BY ta.nombre
    """)
    suspend fun obtenerAtributosPorProducto(productoId: Long): List<ProductoAtributoConTipo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoAtributo(productoAtributo: ProductoAtributoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoAtributos(productosAtributos: List<ProductoAtributoEntity>)

    @Query("DELETE FROM producto_atributos WHERE productoId = :productoId")
    suspend fun eliminarAtributosDeProducto(productoId: Long)

    @Update
    suspend fun actualizarProductoAtributo(productoAtributo: ProductoAtributoEntity)
}