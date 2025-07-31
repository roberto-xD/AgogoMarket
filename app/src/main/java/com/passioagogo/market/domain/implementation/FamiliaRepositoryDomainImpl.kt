package com.passioagogo.market.domain.implementation

import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.repository.IFamiliaRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamiliaRepositoryDomainImpl @Inject constructor(
    private val familiaDao: FamiliaDao
) : IFamiliaRepository {

    override suspend fun obtenerTodasLasFamilias(): Flow<List<Familia>> {
        return familiaDao.obtenerFamiliasActivas().map { entities ->
            entities.map { Familia.fromEntity(it) }
        }
    }

    override suspend fun obtenerFamiliaPorId(id: Long): Familia? {
        return familiaDao.obtenerFamiliaPorId(id)?.let { Familia.fromEntity(it) }
    }

    override suspend fun guardarFamilia(familia: Familia): PADomainState<Long> {
        return try {
            val id = familiaDao.insertarFamilia(familia.toEntity())
            PADomainState.Success(id)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarFamilia(familia: Familia): PADomainState<Unit> {
        return try {
            familiaDao.actualizarFamilia(familia.toEntity())
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarFamilia(id: Long): PADomainState<Unit> {
        return try {
            familiaDao.eliminarFamilia(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }
}