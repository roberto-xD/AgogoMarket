package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoriaDao {
    // Métodos originales
    @Query("SELECT * FROM subcategorias WHERE activo = 1 ORDER BY nombre")
    fun obtenerSubcategoriasActivas(): Flow<List<SubcategoriaEntity>>

    @Query("SELECT * FROM subcategorias WHERE categoriaId = :categoriaId AND activo = 1 ORDER BY nombre")
    fun obtenerSubcategoriasPorCategoria(categoriaId: Long): Flow<List<SubcategoriaEntity>>

    @Query("SELECT * FROM subcategorias WHERE id = :id ")
    suspend fun obtenerSubcategoriaPorId(id: Long): SubcategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSubcategoria(subcategoria: SubcategoriaEntity): Long

    @Update
    suspend fun actualizarSubcategoria(subcategoria: SubcategoriaEntity)

    @Query("UPDATE subcategorias SET activo = 0 WHERE id = :id")
    suspend fun eliminarSubcategoria(id: Long)

}