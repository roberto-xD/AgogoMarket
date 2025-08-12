package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow

interface IProductoRepository {
    suspend fun obtenerTodosLosProductos(): Flow<List<Producto>>
    suspend fun obtenerProductoPorId(id: Long): Producto?
    suspend fun obtenerProductoPorSku(sku: String): Producto?
    suspend fun obtenerProductoPorCodigoBarras(codigo: String): Producto?
    suspend fun obtenerProductosConStockBajo(): Flow<List<Producto>>
    suspend fun obtenerProductosPorCategoria(categoriaId: Long): Flow<List<Producto>>
    suspend fun obtenerProductoDetallado(id: Long): ProductoDetallado?

    suspend fun guardarProducto(producto: Producto): PADomainState<Long>
    suspend fun guardarProductoDetallado(productoDetallado: ProductoDetallado): PADomainState<Long>
    suspend fun actualizarProducto(producto: Producto): PADomainState<Unit>
    suspend fun actualizarProductoDetallado(productoDetallado: ProductoDetallado): PADomainState<Unit>
    suspend fun eliminarProducto(id: Long): PADomainState<Unit>

    suspend fun actualizarStock(id: Long, nuevaCantidad: Int): PADomainState<Unit>
    suspend fun reducirStock(id: Long, cantidad: Int): PADomainState<Unit>
    suspend fun aumentarStock(id: Long, cantidad: Int): PADomainState<Unit>

    suspend fun registrarVenta(id: Long, cantidad: Int): PADomainState<Unit>
    suspend fun registrarCompra(id: Long, cantidad: Int): PADomainState<Unit>

    suspend fun buscarProductos(query: String): Flow<List<Producto>>
}