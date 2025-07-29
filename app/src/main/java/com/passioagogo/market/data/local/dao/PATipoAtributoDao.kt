package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.dinamics.TipoAtributoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoAtributoDao {
    @Query("SELECT * FROM tipos_atributos WHERE activo = 1 ORDER BY nombre")
    fun obtenerTiposAtributosActivos(): Flow<List<TipoAtributoEntity>>

    @Insert
    suspend fun insertarTipoAtributo(tipoAtributo: TipoAtributoEntity): Long

    @Update
    suspend fun actualizarTipoAtributo(tipoAtributo: TipoAtributoEntity)
}