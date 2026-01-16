package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.domain.repository.IProveedorRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProveedorRepositoryDomainImpl @Inject constructor(
    private val proveedorDao: ProveedorDao,
    private val productoProveedorDao: ProductoProveedorDao,
) : IProveedorRepository {

    companion object {
        private const val TAG = "ProveedorRepository"
    }

    override suspend fun obtenerTodosLosProveedores(): Flow<List<Proveedor>> {
        return proveedorDao.obtenerProveedoresActivos().map { entities ->
            entities.map { Proveedor.fromEntity(it) }
        }
    }

    override suspend fun obtenerProveedorPorId(id: Long): Proveedor? {
        return proveedorDao.obtenerProveedorPorId(id)?.let { Proveedor.fromEntity(it) }
    }

    override suspend fun obtenerProveedoresPorProducto(productoId: Long): List<ProveedorConPrecio> {
        return productoProveedorDao.obtenerProveedoresPorProducto(productoId).mapNotNull { proveedorConNombre ->
            proveedorDao.obtenerProveedorPorId(proveedorConNombre.proveedorId)?.let { proveedorEntity ->
                ProveedorConPrecio(
                    proveedor = Proveedor.fromEntity(proveedorEntity),
                    precioCompra = proveedorConNombre.precioCompra,
                    fechaUltimaCompra = proveedorConNombre.fechaUltimaCompra,
                    activo = proveedorConNombre.activo
                )
            }
        }
    }

    override suspend fun guardarProveedor(proveedor: Proveedor): PADomainState<Long> {
        return try {
            val entity = proveedor.toEntity()
            val id = proveedorDao.insertarProveedor(entity)
            PADomainState.Success(id)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarProveedor(proveedor: Proveedor): PADomainState<Unit> {
        return try {
            val entity = proveedor.toEntity().copy(
                updatedAt = System.currentTimeMillis(),
            )
            proveedorDao.actualizarProveedor(entity)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarProveedor(id: Long): PADomainState<Unit> {
        return try {
            proveedorDao.eliminarProveedor(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun asociarProveedorConProducto(
        productoId: Long,
        proveedorId: Long,
        precioCompra: Double
    ): PADomainState<Unit> {
        return try {
            val productoProveedor = ProductoProveedorEntity(
                productoId = productoId,
                proveedorId = proveedorId,
                precioCompra = precioCompra,
                fechaUltimaCompra = System.currentTimeMillis(),
            )
            productoProveedorDao.insertarProductoProveedor(productoProveedor)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
}