package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDao {
    // Métodos originales
    @Query("SELECT * FROM proveedores WHERE activo = 1 ORDER BY nombre")
    fun obtenerProveedoresActivos(): Flow<List<ProveedorEntity>>

    @Query("SELECT * FROM proveedores WHERE id = :id ")
    suspend fun obtenerProveedorPorId(id: Long): ProveedorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProveedor(proveedor: ProveedorEntity): Long

    @Update
    suspend fun actualizarProveedor(proveedor: ProveedorEntity)

    @Query("UPDATE proveedores SET activo = 0 WHERE id = :id")
    suspend fun eliminarProveedor(id: Long)
}