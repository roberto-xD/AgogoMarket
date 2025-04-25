package com.passioagogo.market.domain.usecase

import android.util.Log
import com.passioagogo.market.data.local.entity.toEntity
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.repository.PADBRepository
import com.passioagogo.market.domain.state.PADomainState
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
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class PAProductInfoUseCase @Inject constructor(
    private val repository: PADBRepository
){
    fun getAllProduct() = repository.getAllProduct()

    fun searchProductsByName(searchTerm: String) : Flow<PADomainState<PAProductBean>> = flow {
        val query = repository.searchProductsByName(searchTerm).last()
        if(query.isNotEmpty()){
            PADomainState.Success(query)
        }else{
            PADomainState.Error("No data found")
        }
    }

    suspend fun addNewProduct(product: PAProductBean){
        Log.i(TAG_PG,"use case: ${product.toEntity()}")
        repository.insertProduct(product.toEntity())
    }
}