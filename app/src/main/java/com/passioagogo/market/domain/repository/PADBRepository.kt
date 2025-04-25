package com.passioagogo.market.domain.repository

import kotlinx.coroutines.flow.Flow
import com.passioagogo.market.data.local.entity.PAProductEntity

interface PADBRepository {
    fun getAllProduct() : Flow<List<PAProductEntity>>
}