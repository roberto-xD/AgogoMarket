package com.passioagogo.market.data.repository
import com.passioagogo.market.data.local.dao.PAProductsDaoCRUD
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.repository.PADBRepositoryCRUD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PARepositoryCRUDImpl @Inject constructor(
    private val dao : PAProductsDaoCRUD
): PADBRepositoryCRUD {

    override suspend fun insertProduct(
        product: PAProductEntity
    ): Flow<Long> = flow{
        dao.insertProduct(product)
    }

    override fun deleteProduct(
        id: Int
    ): Flow<Int> = flow{
        dao.deleteProduct(id)
    }

    override fun updateProduct(
        product: PAProductEntity
    ): Flow<Int> = flow{
        dao.updateProduct(product)
    }

    override fun getAllProduct(): Flow<List<PAProductEntity>>{
        return dao.getAllProducts()
    }

    override fun searchProductsByName(
        searchTerm: String
    ): Flow<List<PAProductEntity>> {
        return dao.searchProductsByName(searchTerm)
    }
}