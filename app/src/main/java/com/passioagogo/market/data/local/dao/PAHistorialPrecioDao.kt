package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.passioagogo.market.data.local.entity.utils.HistorialPrecioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialPrecioDao {
    @Query("SELECT * FROM historial_precios WHERE productoId = :productoId ORDER BY fechaCambio DESC")
    fun obtenerHistorialPorProducto(productoId: Long): Flow<List<HistorialPrecioEntity>>

    @Insert
    suspend fun insertarHistorialPrecio(historial: HistorialPrecioEntity)
}