package com.passioagogo.market.domain.repository

import kotlinx.coroutines.flow.Flow
import com.passioagogo.market.data.local.entity.PAProductEntity

interface PADBRepository {
    fun getAllProduct() : Flow<List<PAProductEntity>>
    fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>>
    suspend fun insertProduct(product: PAProductEntity): Flow<Int>
    fun updateProduct(product: PAProductEntity): Flow<Int>
    fun deleteProduct(id: Int): Flow<Int>

}