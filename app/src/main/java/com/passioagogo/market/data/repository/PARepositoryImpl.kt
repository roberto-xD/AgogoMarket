package com.passioagogo.market.data.repository
import com.passioagogo.market.data.local.dao.PAProductsDao
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PARepositoryImpl @Inject constructor(
    private val dao : PAProductsDao
): PADBRepository {

    override fun getAllProduct(): Flow<List<PAProductEntity>> = flow{
        val cuac = dao.getProducts(productId = 1)
        emit(cuac)
    }
}