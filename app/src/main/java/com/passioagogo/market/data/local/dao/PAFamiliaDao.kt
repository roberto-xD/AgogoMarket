package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamiliaDao {
    @Query("SELECT * FROM familias WHERE activo = 1 ORDER BY nombre")
    fun obtenerFamiliasActivas(): Flow<List<FamiliaEntity>>

    @Query("SELECT * FROM familias WHERE id = :id")
    suspend fun obtenerFamiliaPorId(id: Long): FamiliaEntity?

    @Insert
    suspend fun insertarFamilia(familia: FamiliaEntity): Long

    @Update
    suspend fun actualizarFamilia(familia: FamiliaEntity)

    @Query("UPDATE familias SET activo = 0 WHERE id = :id")
    suspend fun eliminarFamilia(id: Long)
}