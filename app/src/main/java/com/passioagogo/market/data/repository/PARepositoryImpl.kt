package com.passioagogo.market.data.repository
import android.util.Log
import com.passioagogo.market.data.local.dao.PAProductsDao
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PARepositoryImpl @Inject constructor(
    private val dao : PAProductsDao
): PADBRepository {

    private val searchQuery = MutableStateFlow("")
    private val debouncePeriod = 300L

    override fun getAllProduct(): Flow<List<PAProductEntity>> = flow{
        dao.getAllProducts()
    }

    override fun searchProductsByName(searchTerm: String): Flow<List<PAProductEntity>> = flow {
        searchQuery.value = searchTerm
        searchQuery
            .debounce(debouncePeriod)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                dao.searchProductsByName(query)
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun insertProduct(product: PAProductEntity): Flow<Int> = flow{
        Log.i(TAG_PG,"repository: ${product}")
        dao.insertProduct(product)
    }

    override fun updateProduct(product: PAProductEntity): Flow<Int> = flow{
        dao.updateProduct(product)
    }

    override fun deleteProduct(id: Int): Flow<Int> = flow{
        dao.deleteProduct(id)
    }
}