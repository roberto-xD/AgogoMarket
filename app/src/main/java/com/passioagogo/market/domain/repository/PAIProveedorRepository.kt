package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow

interface IProveedorRepository {
    suspend fun obtenerTodosLosProveedores(): Flow<List<Proveedor>>
    suspend fun obtenerProveedorPorId(id: Long): Proveedor?
    suspend fun obtenerProveedoresPorProducto(productoId: Long): List<ProveedorConPrecio>
    suspend fun guardarProveedor(proveedor: Proveedor): PADomainState<Long>
    suspend fun actualizarProveedor(proveedor: Proveedor): PADomainState<Unit>
    suspend fun eliminarProveedor(id: Long): PADomainState<Unit>
    suspend fun asociarProveedorConProducto(
        productoId: Long,
        proveedorId: Long,
        precioCompra: Double
    ): PADomainState<Unit>
}