package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.HistorialPrecio
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow

interface IHistorialRepository {
    suspend fun obtenerHistorialPorProducto(productoId: Long): Flow<List<HistorialPrecio>>
    suspend fun registrarCambioPrecio(historial: HistorialPrecio): PADomainState<Unit>
}