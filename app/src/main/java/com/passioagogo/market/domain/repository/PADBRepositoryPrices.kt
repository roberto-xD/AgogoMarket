package com.passioagogo.market.domain.repository

import com.passioagogo.market.data.local.entity.PAPriceEntity
import kotlinx.coroutines.flow.Flow

interface PADBRepositoryPrices {
    suspend fun insertPrice(price: PAPriceEntity):Flow<Long>
    fun updatePrice (price: PAPriceEntity) : Flow<Int>
    fun deletePrice (id: Int): Flow<Int>
    fun getPriceOfProduct(productId: Int): Flow<PAPriceEntity>
}