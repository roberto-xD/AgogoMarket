package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.entity.base.ProductoEntity
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    fun buscarProductos(searchTerm: String): Flow<List<ProductoEntity>>
    fun obtenerProductosActivos(): Flow<List<ProductoEntity>>
    suspend fun obtenerProductoPorId(id: Long): ProductoEntity?
    suspend fun obtenerProductoPorCodigoBarras(codigo: String): ProductoEntity?
    suspend fun obtenerProductoPorSku(sku: String): ProductoEntity?
    fun obtenerProductosConStockBajo(): Flow<List<ProductoEntity>>
    fun obtenerProductosPorCategoria(categoriaId: Long): Flow<List<ProductoEntity>>
    suspend fun insertarProducto(producto: ProductoEntity): Long
    suspend fun actualizarProducto(producto: ProductoEntity)
    suspend fun eliminarProducto(id: Long)
    suspend fun actualizarStock(id: Long, nuevaCantidad: Int)
    suspend fun actualizarFechaUltimaVenta(id: Long, fecha: Long)
    suspend fun actualizarFechaUltimaCompra(id: Long, fecha: Long)

    // Métodos relacionales
    suspend fun obtenerProductoCompleto(id: Long): ProductoCompleto?
    suspend fun guardarProductoCompleto(productoCompleto: ProductoCompleto): Long
    suspend fun actualizarProductoCompleto(productoCompleto: ProductoCompleto)
}