package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamiliaRepositoryImpl @Inject constructor(
    private val familiaDao: FamiliaDao
) : FamiliaRepository {

    override fun obtenerFamiliasActivas(): Flow<List<FamiliaEntity>> =
        familiaDao.obtenerFamiliasActivas()

    override suspend fun obtenerFamiliaPorId(id: Long): FamiliaEntity? =
        familiaDao.obtenerFamiliaPorId(id)

    override suspend fun insertarFamilia(familia: FamiliaEntity): Long =
        familiaDao.insertarFamilia(familia)

    override suspend fun actualizarFamilia(familia: FamiliaEntity) =
        familiaDao.actualizarFamilia(familia)

    override suspend fun eliminarFamilia(id: Long) =
        familiaDao.eliminarFamilia(id)
}