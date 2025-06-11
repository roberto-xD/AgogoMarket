package com.passioagogo.market.domain.repository

import com.passioagogo.market.data.local.entity.PAProductEntity
import kotlinx.coroutines.flow.Flow

interface PADBRepositoryCRUD {
    suspend fun insertProduct(product: PAProductEntity): Flow<Long>
    fun updateProduct(product: PAProductEntity): Flow<Int>
    fun deleteProduct(id: Int): Flow<Int>
    fun getAllProduct() : Flow<List<PAProductEntity>>
    fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>>
}