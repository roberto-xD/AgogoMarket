package com.passioagogo.market.domain.usecase

import com.passioagogo.market.data.local.entity.PAStatistics
import com.passioagogo.market.data.local.converters.toEntity
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PASaveLocalProductWithStatistics @Inject constructor(
    private val repository: PADBRepository
){
    suspend operator fun invoke (product: PAProductBean, statistics: PAStatistics): Flow<Long> {
        return repository.insertProductWithStatistics(product.toEntity(), statistics)
    }
}