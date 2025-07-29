package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
import com.passioagogo.market.data.local.entity.utils.ProductoProveedorConNombre
import kotlinx.coroutines.flow.Flow

interface ProveedorRepository {
    fun obtenerProveedoresActivos(): Flow<List<ProveedorEntity>>
    suspend fun obtenerProveedorPorId(id: Long): ProveedorEntity?
    suspend fun insertarProveedor(proveedor: ProveedorEntity): Long
    suspend fun actualizarProveedor(proveedor: ProveedorEntity)
    suspend fun eliminarProveedor(id: Long)
    suspend fun obtenerProveedoresPorProducto(productoId: Long): List<ProductoProveedorConNombre>
    suspend fun asociarProveedorConProducto(productoProveedor: ProductoProveedorEntity)
}