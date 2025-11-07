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

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM producto_proveedores WHERE productoId = :productoId AND proveedorId = :proveedorId")
    suspend fun obtenerRelacion(productoId: Long, proveedorId: Long): ProductoProveedorEntity?

    @Query("SELECT * FROM producto_proveedores WHERE productoId = :productoId")
    suspend fun obtenerRelacionesPorProducto(productoId: Long): List<ProductoProveedorEntity>

    @Query("SELECT * FROM producto_proveedores")
    suspend fun obtenerTodasLasRelaciones(): List<ProductoProveedorEntity>

    @Query("SELECT * FROM producto_proveedores WHERE needsSync = 1")
    suspend fun getRelacionesPendientesSync(): List<ProductoProveedorEntity>

    @Query("UPDATE producto_proveedores SET productoRemoteId = :productoRemoteId, proveedorRemoteId = :proveedorRemoteId, isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND proveedorId = :proveedorId")
    suspend fun actualizarRemoteIds(productoId: Long, proveedorId: Long, productoRemoteId: String, proveedorRemoteId: String)

    @Query("UPDATE producto_proveedores SET isSynced = 1, needsSync = 0 WHERE productoId = :productoId AND proveedorId = :proveedorId")
    suspend fun marcarComoSincronizada(productoId: Long, proveedorId: Long)

    @Query("UPDATE producto_proveedores SET needsSync = 1, isSynced = 0 WHERE productoId = :productoId")
    suspend fun marcarRelacionesDeProductoComoPendientes(productoId: Long)

    @Query("UPDATE producto_proveedores SET needsSync = 1, isSynced = 0, precioCompra = :nuevoPrecio WHERE productoId = :productoId AND proveedorId = :proveedorId")
    suspend fun actualizarPrecioYMarcarSync(productoId: Long, proveedorId: Long, nuevoPrecio: Double)
}