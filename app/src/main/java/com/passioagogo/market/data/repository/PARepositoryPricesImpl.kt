package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.PAProductPricesDao
import com.passioagogo.market.data.local.entity.PAPriceEntity
import com.passioagogo.market.domain.repository.PADBRepositoryPrices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PARepositoryPricesImpl @Inject constructor(
    private val dao : PAProductPricesDao
) :PADBRepositoryPrices{
    override suspend fun insertPrice(price: PAPriceEntity): Flow<Long> = flow{
        dao.insertProductPrice(price)
    }

    override fun updatePrice(price: PAPriceEntity): Flow<Int> = flow{
        dao.updateProductPrice(price)
    }

    override fun deletePrice(id: Int): Flow<Int> = flow{
        dao.deleteProductPrice(id)
    }

    override fun getPriceOfProduct(productId: Int): Flow<PAPriceEntity> = flow {
        dao.getProducts(productId)
    }

}