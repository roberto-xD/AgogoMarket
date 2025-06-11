package com.passioagogo.market.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.PAProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PAProductsDaoCRUD {
    @Query("SELECT * FROM PRODUCT_TABLE WHERE productId LIKE :productId")
    fun getProducts(productId: Int): List<PAProductEntity>

    @Query("SELECT * FROM PRODUCT_TABLE ORDER BY nombre")
    fun getAllProducts(): Flow<List<PAProductEntity>>

    @Query("SELECT * FROM PRODUCT_TABLE WHERE nombre LIKE :searchTerm || '%'")
    fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>>

    @Insert(entity = PAProductEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: PAProductEntity): Long

    @Update(entity = PAProductEntity::class)
    suspend fun updateProduct(producto: PAProductEntity): Int

    @Query("DELETE FROM PRODUCT_TABLE WHERE productId = :id")
    suspend fun deleteProduct(id: Int): Int

    /*@Insert
    suspend fun insertStatistics(statistics: PAStatistics): Long

    @Transaction
    suspend fun insertProductWithStatistics(product: PAProductEntity, statistics: PAStatistics): Long {
        val productId = insertProduct(product)
        statistics.productIdRef = productId
        insertStatistics(statistics)
        return productId
    }

    @Transaction
    @Query("SELECT * FROM PRODUCT_TABLE WHERE productId = :productId")
    suspend fun getProductWithStatistics(productId: Int): PAProductWithStatistics?
*/
}
