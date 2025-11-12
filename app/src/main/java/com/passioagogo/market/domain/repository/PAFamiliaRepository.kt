package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow

interface FamiliaRepository {
    suspend fun obtenerTodasLasFamilias(): Flow<List<Familia>>
    suspend fun obtenerFamiliaPorId(id: Long): Familia?
    suspend fun guardarFamilia(familia: Familia): PADomainState<Long>
    suspend fun actualizarFamilia(familia: Familia): PADomainState<Unit>
    suspend fun eliminarFamilia(id: Long): PADomainState<Unit>
}