package com.passioagogo.market.domain.usecase

import com.passioagogo.market.data.local.converters.toEntity
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PASaveProductLocalUseCase @Inject constructor(
    private val repository: PADBRepository
){
    suspend operator fun invoke (product: PAProductBean): Flow<Long>{
        return repository.insertProduct(product.toEntity())
    }
}