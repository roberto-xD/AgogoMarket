package com.passioagogo.market.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passioagogo.market.data.local.entity.PAPriceEntity

interface PAProductPricesDao {
    @Insert(entity = PAPriceEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertProductPrice(productPrice: PAPriceEntity): Long

    @Update(entity = PAPriceEntity::class)
    suspend fun updateProductPrice(productoPrice: PAPriceEntity): Int

    @Query("DELETE FROM PRICE_TABLE WHERE productId = :id")
    suspend fun deleteProductPrice(id: Int): Int

    @Query("SELECT * FROM PRICE_TABLE WHERE productId LIKE :productId")
    fun getProducts(productId: Int): List<PAPriceEntity>
}