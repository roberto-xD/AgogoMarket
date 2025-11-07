package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passioagogo.market.data.local.entity.venta.VentaConProductos
import com.passioagogo.market.data.local.entity.venta.VentaEntity
import com.passioagogo.market.data.local.entity.venta.VentaProductoEntity

@Dao
interface VentaDao {

    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerVentaConProductos(id: Long): VentaConProductos?

    @Transaction
    @Query("SELECT * FROM ventas WHERE isDeleted = 0 ORDER BY fechaCreacion DESC")
    suspend fun obtenerTodasVentasConProductos(): List<VentaConProductos>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM ventas WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerVentaPorId(id: Long): VentaEntity?

    @Query("SELECT * FROM ventas WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerVentaPorRemoteId(remoteId: String): VentaEntity?

    @Query("SELECT * FROM ventas WHERE isDeleted = 0")
    suspend fun obtenerTodasLasVentas(): List<VentaEntity>

    @Query("SELECT * FROM ventas WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getVentasPendientesSync(): List<VentaEntity>

    @Query("SELECT * FROM ventas WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getVentasNoSincronizadas(): List<VentaEntity>

    @Query("SELECT * FROM ventas WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getVentasEliminadasPendientes(): List<VentaEntity>

    // VentaProducto
    @Query("SELECT * FROM venta_productos WHERE ventaId = :ventaId")
    suspend fun obtenerProductosDeVenta(ventaId: Long): List<VentaProductoEntity>

    @Query("SELECT * FROM venta_productos WHERE remoteId = :remoteId")
    suspend fun obtenerVentaProductoPorRemoteId(remoteId: String): VentaProductoEntity?

    @Query("SELECT * FROM venta_productos WHERE needsSync = 1")
    suspend fun getVentaProductosPendientesSync(): List<VentaProductoEntity>

    @Query("UPDATE ventas SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarVentaComoEliminada(id: Long)

    @Query("DELETE FROM ventas WHERE id = :id")
    suspend fun eliminarVentaPermanentemente(id: Long)

    @Query("DELETE FROM ventas WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarVentasEliminadasSincronizadas()

    @Query("UPDATE ventas SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarVentaRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE venta_productos SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarVentaProductoRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE venta_productos SET ventaRemoteId = :ventaRemoteId, productoRemoteId = :productoRemoteId WHERE id = :localId")
    suspend fun actualizarVentaProductoForeignRemoteIds(localId: Long, ventaRemoteId: String, productoRemoteId: String)

    @Query("UPDATE ventas SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarVentaComoSincronizada(id: Long)

    @Query("UPDATE ventas SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarVentaComoPendienteSync(id: Long)

    @Query("UPDATE venta_productos SET needsSync = 1, isSynced = 0 WHERE ventaId = :ventaId")
    suspend fun marcarProductosDeVentaComoPendientesSync(ventaId: Long)
}