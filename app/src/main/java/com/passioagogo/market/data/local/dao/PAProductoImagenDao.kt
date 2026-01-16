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
    @Query("SELECT * FROM producto_imagenes ")
    suspend fun obtenerTodasLasImagenes(): List<ProductoImagenEntity>

    @Query("SELECT * FROM producto_imagenes WHERE productoId = :productoId ORDER BY orden, fechaCreacion")
    suspend fun obtenerImagenesPorProducto(productoId: Long): List<ProductoImagenEntity>

    @Query("SELECT * FROM producto_imagenes WHERE productoId = :productoId AND esPrincipal = 1 LIMIT 1")
    suspend fun obtenerImagenPrincipal(productoId: Long): ProductoImagenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarImagen(imagen: ProductoImagenEntity): Long

    @Update
    suspend fun actualizarImagen(imagen: ProductoImagenEntity)

    @Query("DELETE FROM producto_imagenes WHERE id = :id")
    suspend fun eliminarImagen(id: Long)

    @Query("DELETE FROM producto_imagenes WHERE productoId = :productoId")
    suspend fun eliminarImagenesDeProducto(productoId: Long)

}