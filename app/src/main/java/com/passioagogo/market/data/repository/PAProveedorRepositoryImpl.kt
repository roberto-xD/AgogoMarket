package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
import com.passioagogo.market.data.local.entity.utils.ProductoProveedorConNombre
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProveedorRepositoryImpl @Inject constructor(
    private val proveedorDao: ProveedorDao,
    private val productoProveedorDao: ProductoProveedorDao
) : ProveedorRepository {

    override fun obtenerProveedoresActivos(): Flow<List<ProveedorEntity>> =
        proveedorDao.obtenerProveedoresActivos()

    override suspend fun obtenerProveedorPorId(id: Long): ProveedorEntity? =
        proveedorDao.obtenerProveedorPorId(id)

    override suspend fun insertarProveedor(proveedor: ProveedorEntity): Long =
        proveedorDao.insertarProveedor(proveedor)

    override suspend fun actualizarProveedor(proveedor: ProveedorEntity) =
        proveedorDao.actualizarProveedor(proveedor)

    override suspend fun eliminarProveedor(id: Long) =
        proveedorDao.eliminarProveedor(id)

    override suspend fun obtenerProveedoresPorProducto(productoId: Long): List<ProductoProveedorConNombre> =
        productoProveedorDao.obtenerProveedoresPorProducto(productoId)

    override suspend fun asociarProveedorConProducto(productoProveedor: ProductoProveedorEntity) =
        productoProveedorDao.insertarProductoProveedor(productoProveedor)
}