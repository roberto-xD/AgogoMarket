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
    // Métodos originales
    @Transaction
    @Query("SELECT * FROM productos WHERE nombre LIKE '%' || :searchTerm || '%' AND activo = 1 AND isDeleted = 0")
    fun buscarProductos(searchTerm: String): Flow<List<ProductoConImagenes>>

    @Transaction
    @Query("SELECT * FROM productos WHERE activo = 1 AND isDeleted = 0 ORDER BY nombre")
    fun obtenerProductosActivos(): Flow<List<ProductoConImagenes>>

    @Query("SELECT * FROM productos WHERE id = :id AND isDeleted = 0")
    suspend fun obtenerProductoPorId(id: Long): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigoBarras = :codigo AND activo = 1 AND isDeleted = 0")
    suspend fun obtenerProductoPorCodigoBarras(codigo: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE skuInterno = :sku AND activo = 1 AND isDeleted = 0")
    suspend fun obtenerProductoPorSku(sku: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE cantidadActual <= cantidadMinima AND activo = 1 AND isDeleted = 0")
    fun obtenerProductosConStockBajo(): Flow<List<ProductoEntity>>

    @Query("""
        SELECT p.* FROM productos p
        INNER JOIN producto_categorias pc ON p.id = pc.productoId
        WHERE pc.categoriaId = :categoriaId AND p.activo = 1 AND p.isDeleted = 0
        ORDER BY p.nombre
    """)
    fun obtenerProductosPorCategoria(categoriaId: Long): Flow<List<ProductoEntity>>

    @Query("""
        SELECT p.* FROM productos p
        INNER JOIN producto_familia pf ON p.id = pf.productoId
        WHERE pf.familiaId = :familiaId AND p.activo = 1 AND p.isDeleted = 0
        ORDER BY p.nombre
    """)
    fun obtenerProductosPorFamilia(familiaId: Long): Flow<List<ProductoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    // ========== NUEVOS MÉTODOS PARA SINCRONIZACIÓN ==========

    @Query("SELECT * FROM productos WHERE remoteId = :remoteId AND isDeleted = 0")
    suspend fun obtenerProductoPorRemoteId(remoteId: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE isDeleted = 0")
    suspend fun obtenerTodosLosProductos(): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE needsSync = 1 AND isDeleted = 0")
    suspend fun getProductosPendientesSync(): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE remoteId IS NULL AND isDeleted = 0")
    suspend fun getProductosNoSincronizados(): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getProductosEliminadosPendientes(): List<ProductoEntity>

    @Query("UPDATE productos SET isDeleted = 1, needsSync = 1 WHERE id = :id")
    suspend fun marcarComoEliminado(id: Long)

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarPermanentemente(id: Long)

    @Query("DELETE FROM productos WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun limpiarProductosEliminadosSincronizados()

    @Query("UPDATE productos SET remoteId = :remoteId, isSynced = 1, needsSync = 0 WHERE id = :localId")
    suspend fun actualizarRemoteId(localId: Long, remoteId: String)

    @Query("UPDATE productos SET proveedorPrincipalRemoteId = :proveedorRemoteId WHERE id = :localId")
    suspend fun actualizarProveedorPrincipalRemoteId(localId: Long, proveedorRemoteId: String)

    @Query("UPDATE productos SET isSynced = 1, needsSync = 0 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("UPDATE productos SET needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun marcarComoPendienteSync(id: Long)

    // Actualizar stock y marcar como pendiente de sincronización
    @Query("UPDATE productos SET cantidadActual = :nuevaCantidad, fechaActualizacion = :fecha, needsSync = 1, isSynced = 0 WHERE id = :id")
    suspend fun actualizarStockYMarcarSync(id: Long, nuevaCantidad: Int, fecha: Long = System.currentTimeMillis())
}