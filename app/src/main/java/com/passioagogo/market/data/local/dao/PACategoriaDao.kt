package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    // Métodos originales
    @Query("SELECT * FROM categorias WHERE activo = 1 ORDER BY nombre")
    fun obtenerCategoriasActivas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE familiaId = :familiaId AND activo = 1 ORDER BY nombre")
    fun obtenerCategoriasPorFamiliaId(familiaId: Long): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun obtenerCategoriaPorId(id: Long): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategoria(categoria: CategoriaEntity): Long

    @Update
    suspend fun actualizarCategoria(categoria: CategoriaEntity)

    @Query("UPDATE categorias SET activo = 0 WHERE id = :id")
    suspend fun eliminarCategoria(id: Long)

}