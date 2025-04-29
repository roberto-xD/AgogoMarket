package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.PAProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PAProductsDao {
    @Query("SELECT * FROM PRODUCT_TABLE WHERE id LIKE :productId")
    fun getProducts(productId: Int): List<PAProductEntity>

    @Query("SELECT * FROM PRODUCT_TABLE ORDER BY nombre")
    fun getAllProducts(): Flow<List<PAProductEntity>>

    @Query("SELECT * FROM PRODUCT_TABLE WHERE nombre LIKE :searchTerm || '%'")
    fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>>

    @Insert(entity = PAProductEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: PAProductEntity): Long
    @Update(entity = PAProductEntity::class)
    suspend fun updateProduct(producto: PAProductEntity): Int

    @Query("DELETE FROM PRODUCT_TABLE WHERE id = :id")
    suspend fun deleteProduct(id: Int): Int


//    @Query("SELECT * FROM PRODUCT_TABLE WHERE precio BETWEEN :minPrice AND :maxPrice")

//    @Query(
//        """
//        SELECT * FROM PRODUCT_TABLE
//        WHERE nombre LIKE '%' || :searchTerm || '%'
//        AND categoria = :categoria
//        ORDER BY precio DESC
//        LIMIT :limit
//        """
//    )
//    fun buscarProductosCombinado(
//        searchTerm: String,
//        categoria: String,
//        limit: Int
//    ): Flow<List<PAProductEntity>>
}
