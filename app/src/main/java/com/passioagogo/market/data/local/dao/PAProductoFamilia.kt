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
}