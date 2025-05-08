package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.PADBRepository
import javax.inject.Inject

class PAGetLocalProductWithStatistics @Inject constructor(
    private val repository: PADBRepository
){
    operator fun invoke (productId: Int) = repository.getProductWithStatistics(productId)
}