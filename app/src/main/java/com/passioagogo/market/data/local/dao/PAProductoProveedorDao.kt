package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
import com.passioagogo.market.data.local.entity.utils.ProductoProveedorConNombre

@Dao
interface ProductoProveedorDao {
    @Query("""
        SELECT pp.*, p.nombre as nombreProveedor FROM producto_proveedores pp
        INNER JOIN proveedores p ON pp.proveedorId = p.id
        WHERE pp.productoId = :productoId AND pp.activo = 1
        ORDER BY p.nombre
    """)
    suspend fun obtenerProveedoresPorProducto(productoId: Long): List<ProductoProveedorConNombre>

    @Query("""
        SELECT pp.precioCompra FROM producto_proveedores pp
        WHERE pp.productoId = :productoId AND pp.proveedorId = :proveedorId AND pp.activo = 1
    """)
    suspend fun obtenerPrecioProveedorProducto(productoId: Long, proveedorId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductoProveedor(productoProveedor: ProductoProveedorEntity)

    @Update
    suspend fun actualizarProductoProveedor(productoProveedor: ProductoProveedorEntity)

    @Query("UPDATE producto_proveedores SET activo = 0 WHERE productoId = :productoId AND proveedorId = :proveedorId")
    suspend fun eliminarProductoProveedor(productoId: Long, proveedorId: Long)
}