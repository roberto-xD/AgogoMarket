package com.passioagogo.market.data.repository

//import com.passioagogo.market.data.mapper.toCreateDto
//import com.passioagogo.market.data.mapper.toEntity
import android.util.Log
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.repository.FamiliaRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamiliaRepositoryDomainImpl @Inject constructor(
    private val familiaDao: FamiliaDao,
) : FamiliaRepository {

    companion object {
        private const val TAG = "FamiliaRepository"
    }

    // ========================================
    // OPERACIONES LOCALES (MANTIENEN TU LÓGICA ORIGINAL)
    // ========================================

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
            // Convertir a Entity y agregar campos de sincronización
            val entity = familia.toEntity()
            val id = familiaDao.insertarFamilia(entity)
            PADomainState.Success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar familia: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarFamilia(familia: Familia): PADomainState<Unit> {
        return try {
            // Obtener entity existente para preservar campos de sincronización
            familiaDao.obtenerFamiliaPorId(familia.id)?.let {
                familiaDao.actualizarFamilia(it)
            }
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar familia: ${e.message}")
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarFamilia(id: Long): PADomainState<Unit> {
        return try {
            // Soft delete
            familiaDao.eliminarFamilia(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar familia: ${e.message}")
            PADomainState.Error(e)
        }
    }
}