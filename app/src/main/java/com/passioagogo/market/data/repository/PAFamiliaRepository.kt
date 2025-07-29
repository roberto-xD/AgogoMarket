package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import kotlinx.coroutines.flow.Flow

interface FamiliaRepository {
    fun obtenerFamiliasActivas(): Flow<List<FamiliaEntity>>
    suspend fun obtenerFamiliaPorId(id: Long): FamiliaEntity?
    suspend fun insertarFamilia(familia: FamiliaEntity): Long
    suspend fun actualizarFamilia(familia: FamiliaEntity)
    suspend fun eliminarFamilia(id: Long)
}