package com.passioagogo.market.data.repository

import android.util.Log
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
//import com.passioagogo.market.data.mapper.toCreateDto
//import com.passioagogo.market.data.mapper.toEntity
import com.passioagogo.market.data.remote.dto.ProveedorDto
import com.passioagogo.market.data.repository.sync.SyncHelper
import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.domain.repository.IProveedorRepository
import com.passioagogo.market.domain.state.PADomainState
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProveedorRepositoryDomainImpl @Inject constructor(
    private val proveedorDao: ProveedorDao,
    private val productoProveedorDao: ProductoProveedorDao,
    private val auth: Auth,
    private val postgrest: Postgrest
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
            val userId = auth.currentUserOrNull()?.id

//            val entity = proveedor.toEntity().copy(
//                userId = userId,
//                needsSync = true,
//                isSynced = false
//            )

//            val id = proveedorDao.insertarProveedor(entity)
//
//            if (userId != null) {
//                try {
//                    sincronizarProveedorIndividual(id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar: ${e.message}")
//                }
//            }

            PADomainState.Success(0)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarProveedor(proveedor: Proveedor): PADomainState<Unit> {
        return try {
            val entityExistente = proveedorDao.obtenerProveedorPorId(proveedor.id)

//            val entity = proveedor.toEntity().copy(
//                remoteId = entityExistente?.remoteId,
//                userId = entityExistente?.userId,
//                updatedAt = System.currentTimeMillis(),
//                needsSync = true,
//                isSynced = false
//            )

//            proveedorDao.actualizarProveedor(entity)
//
//            if (entity.remoteId != null) {
//                try {
//                    sincronizarProveedorIndividual(proveedor.id)
//                } catch (e: Exception) {
//                    Log.w(TAG, "No se pudo sincronizar: ${e.message}")
//                }
//            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarProveedor(id: Long): PADomainState<Unit> {
        return try {
//            proveedorDao.marcarComoEliminado(id)
//
//            try {
//                val proveedor = proveedorDao.obtenerProveedorPorId(id)
//                if (proveedor?.remoteId != null) {
//                    eliminarEnSupabase(proveedor.remoteId)
//                    proveedorDao.eliminarPermanentemente(id)
//                }
//            } catch (e: Exception) {
//                Log.w(TAG, "No se pudo eliminar en Supabase: ${e.message}")
//            }

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
//                needsSync = true,
//                isSynced = false
            )
            productoProveedorDao.insertarProductoProveedor(productoProveedor)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
//
//    // Métodos privados de sincronización
//    private suspend fun sincronizarProveedorIndividual(localId: Long) {
//        val entity = proveedorDao.obtenerProveedorPorId(localId) ?: return
//        val userId = SyncHelper.requireUserId(entity.userId)
//
//        if (entity.remoteId == null) {
//            crearEnSupabase(entity, userId)
//        } else {
//            actualizarEnSupabase(entity)
//        }
//    }
//
//    private suspend fun crearEnSupabase(entity: com.passioagogo.market.data.local.entity.base.ProveedorEntity, userId: String) {
//        SyncHelper.withRetry {
//            val createDto = entity.copy(userId = userId).toCreateDto()
//
//            val createdDto = postgrest.from("proveedores")
//                .insert(createDto) {
//                    select()
//                }
//                .decodeSingle<ProveedorDto>()
//
//            proveedorDao.actualizarRemoteId(entity.id, createdDto.id)
//        }
//    }
//
//    private suspend fun actualizarEnSupabase(entity: com.passioagogo.market.data.local.entity.base.ProveedorEntity) {
//        if (entity.remoteId == null) return
//
//        SyncHelper.withRetry {
//            postgrest.from("proveedores")
//                .update({
//                    set("nombre", entity.nombre)
//                    set("contacto", entity.contacto)
//                    set("telefono", entity.telefono)
//                    set("email", entity.email)
//                    set("direccion", entity.direccion)
//                    set("activo", entity.activo)
//                }) {
//                    filter {
//                        eq("id", entity.remoteId)
//                    }
//                }
//
//            proveedorDao.marcarComoSincronizado(entity.id)
//        }
//    }

    private suspend fun eliminarEnSupabase(remoteId: String) {
        SyncHelper.withRetry {
            postgrest.from("proveedores")
                .delete {
                    filter {
                        eq("id", remoteId)
                    }
                }
        }
    }
}