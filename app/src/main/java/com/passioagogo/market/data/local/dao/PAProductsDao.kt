package com.passioagogo.market.data.local.dao
import androidx.room.Dao
import androidx.room.Query
import com.passioagogo.market.data.local.entity.PAProductEntity

@Dao
interface PAProductsDao {
    @Query("SELECT * FROM PRODUCT_TABLE WHERE id LIKE :productId")
    fun getProducts(productId: Int) : List<PAProductEntity>

}