package com.passioagogo.market.domain.repository

import kotlinx.coroutines.flow.Flow
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.data.local.relation.PAProductWithStatistics
import com.passioagogo.market.data.local.entity.PAStatistics

interface PADBRepository {
    fun getAllProduct() : Flow<List<PAProductEntity>>
    fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>>
    fun getProductWithStatistics(productId: Int): Flow<PAProductWithStatistics?>

    suspend fun insertProduct(product: PAProductEntity): Flow<Long>
    suspend fun insertProductWithStatistics(product: PAProductEntity, statistics: PAStatistics): Flow<Long>

    fun updateProduct(product: PAProductEntity): Flow<Int>
    fun deleteProduct(id: Int): Flow<Int>

}