package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.ProductoConImagenes
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos WHERE nombre LIKE :searchTerm || '%'")
    fun buscarProductos(searchTerm: String): Flow<List<ProductoConImagenes>>

    @Transaction
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre")
    fun obtenerProductosActivos(): Flow<List<ProductoConImagenes>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerProductoPorId(id: Long): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigoBarras = :codigo AND activo = 1")
    suspend fun obtenerProductoPorCodigoBarras(codigo: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE skuInterno = :sku AND activo = 1")
    suspend fun obtenerProductoPorSku(sku: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE cantidadActual <= cantidadMinima AND activo = 1")
    fun obtenerProductosConStockBajo(): Flow<List<ProductoEntity>>

    @Query("""
        SELECT p.* FROM productos p
        INNER JOIN producto_categorias pc ON p.id = pc.productoId
        WHERE pc.categoriaId = :categoriaId AND p.activo = 1
        ORDER BY p.nombre
    """)
    fun obtenerProductosPorCategoria(categoriaId: Long): Flow<List<ProductoEntity>>

    @Query("""
        SELECT p.* FROM productos p
        INNER JOIN producto_familia pc ON p.id = pc.productoId
        WHERE pc.familiaId = :familiaId AND p.activo = 1
        ORDER BY p.nombre
    """)
    fun obtenerProductosPorFamilia(familiaId: Long): Flow<List<ProductoEntity>>

    @Insert
    suspend fun insertarProducto(producto: ProductoEntity): Long

    @Update
    suspend fun actualizarProducto(producto: ProductoEntity)

    @Query("UPDATE productos SET activo = 0 WHERE id = :id")
    suspend fun eliminarProducto(id: Long)

    @Query("UPDATE productos SET cantidadActual = :nuevaCantidad, fechaActualizacion = :fecha WHERE id = :id")
    suspend fun actualizarStock(id: Long, nuevaCantidad: Int, fecha: Long = System.currentTimeMillis())

    @Query("UPDATE productos SET fechaUltimaVenta = :fecha, fechaActualizacion = :fechaActualizacion WHERE id = :id")
    suspend fun actualizarFechaUltimaVenta(id: Long, fecha: Long, fechaActualizacion: Long = System.currentTimeMillis())

    @Query("UPDATE productos SET fechaUltimaCompra = :fecha, fechaActualizacion = :fechaActualizacion WHERE id = :id")
    suspend fun actualizarFechaUltimaCompra(id: Long, fecha: Long, fechaActualizacion: Long = System.currentTimeMillis())
}