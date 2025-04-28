package com.passioagogo.market.data.repository
import com.passioagogo.market.data.local.dao.PAProductsDao
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PARepositoryImpl @Inject constructor(
    private val dao : PAProductsDao
): PADBRepository {

    override fun getAllProduct(): Flow<List<PAProductEntity>>{
        return dao.getAllProducts()
    }

    override fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>> {
        return dao.searchProductsByName(searchTerm)
    }

    override suspend fun insertProduct(product: PAProductEntity): Flow<Long>{
        return dao.insertProduct(product)
    }

    override fun updateProduct(product: PAProductEntity): Flow<Int> = flow{
        dao.updateProduct(product)
    }

    override fun deleteProduct(id: Int): Flow<Int> = flow{
        dao.deleteProduct(id)
    }
}