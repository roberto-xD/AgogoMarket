package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passioagogo.market.data.local.entity.venta.VentaConProductos
import com.passioagogo.market.data.local.entity.venta.VentaEntity
import com.passioagogo.market.data.local.entity.venta.VentaProductoEntity

@Dao
interface VentaDao {

    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun obtenerVentaConProductos(id: Long): VentaConProductos?

    @Transaction
    @Query("SELECT * FROM ventas ORDER BY fechaCreacion DESC")
    suspend fun obtenerTodasVentasConProductos(): List<VentaConProductos>

    @Insert
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertarVentaProductos(productos: List<VentaProductoEntity>)

    @Update
    suspend fun actualizarVenta(venta: VentaEntity)

    @Delete
    suspend fun eliminarVenta(venta: VentaEntity)

    @Query("DELETE FROM venta_productos WHERE ventaId = :ventaId")
    suspend fun eliminarProductosDeVenta(ventaId: Long)

    @Transaction
    suspend fun guardarVentaCompleta(
        venta: VentaEntity,
        productos: List<VentaProductoEntity>
    ): Long {
        val ventaId = insertarVenta(venta)
        val productosConVentaId = productos.map { it.copy(ventaId = ventaId) }
        insertarVentaProductos(productosConVentaId)
        return ventaId
    }

    @Transaction
    suspend fun actualizarVentaCompleta(
        venta: VentaEntity,
        productos: List<VentaProductoEntity>
    ) {
        actualizarVenta(venta)
        eliminarProductosDeVenta(venta.id)
        insertarVentaProductos(productos)
    }
}