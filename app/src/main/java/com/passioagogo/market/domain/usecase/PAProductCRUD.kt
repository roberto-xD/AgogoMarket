package com.passioagogo.market.domain.usecase

import android.util.Log
import com.passioagogo.market.data.local.converters.toEntity
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.repository.PADBRepositoryCRUD
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PASaveProductLocalUseCase @Inject constructor(
    private val repository: PADBRepositoryCRUD
){
    suspend operator fun invoke (product: PAProductBean): Flow<Long>{
        return repository.insertProduct(product.toEntity())
    }
}

class PAGetAllProductLocalUseCase @Inject constructor(
    private val repository: PADBRepositoryCRUD
){
    operator fun invoke(): Flow<List<PAProductEntity>>{
        Log.i(TAG_PG,"on get use case")
        return repository.getAllProduct()
    }
}

class PASearchProductLocalUseCase @Inject constructor(
    private val repository: PADBRepositoryCRUD
){
    operator fun invoke (searchTerm: String) : Flow<List<PAProductEntity>>{
        return repository.searchProductsByName(searchTerm)
    }
}

class PADeleteProductUseCase @Inject constructor(
    private val repository: PADBRepositoryCRUD
){
    operator fun invoke(productId : Int): Flow<Int>{
        return repository.deleteProduct(productId)
    }
}

class PAUpdateProductUseCase @Inject constructor(
    private val repository: PADBRepositoryCRUD
){
    operator fun invoke(product: PAProductBean): Flow<Int>{
        return repository.updateProduct(product.toEntity())
    }
}