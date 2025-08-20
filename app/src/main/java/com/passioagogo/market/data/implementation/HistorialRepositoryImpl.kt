package com.passioagogo.market.data.implementation

import com.passioagogo.market.data.local.dao.HistorialPrecioDao
import com.passioagogo.market.data.local.entity.utils.HistorialPrecioEntity
import com.passioagogo.market.domain.bean.HistorialPrecio
import com.passioagogo.market.domain.repository.IHistorialRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistorialRepositoryImpl @Inject constructor(
    private val historialPrecioDao: HistorialPrecioDao
) : IHistorialRepository {

    override suspend fun obtenerHistorialPorProducto(productoId: Long): Flow<List<HistorialPrecio>> {
        return historialPrecioDao.obtenerHistorialPorProducto(productoId).map { entities ->
            entities.map { entity ->
                HistorialPrecio(
                    id = entity.id,
                    productoId = entity.productoId,
                    precioCompraAnterior = entity.precioCompraAnterior,
                    precioVentaAnterior = entity.precioVentaAnterior,
                    precioCompraNuevo = entity.precioCompraNuevo,
                    precioVentaNuevo = entity.precioVentaNuevo,
                    motivo = entity.motivo,
                    fechaCambio = entity.fechaCambio
                )
            }
        }
    }

    override suspend fun registrarCambioPrecio(historial: HistorialPrecio): PADomainState<Unit> {
        return try {
            val entity = HistorialPrecioEntity(
                id = historial.id,
                productoId = historial.productoId,
                precioCompraAnterior = historial.precioCompraAnterior,
                precioVentaAnterior = historial.precioVentaAnterior,
                precioCompraNuevo = historial.precioCompraNuevo,
                precioVentaNuevo = historial.precioVentaNuevo,
                motivo = historial.motivo,
                fechaCambio = historial.fechaCambio
            )
            historialPrecioDao.insertarHistorialPrecio(entity)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
}