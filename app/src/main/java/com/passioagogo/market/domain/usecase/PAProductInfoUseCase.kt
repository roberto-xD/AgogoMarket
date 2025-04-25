package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class PAProductInfoUseCase @Inject constructor(
    private val repository: PADBRepository
){
    fun getAllProduct() = flow {
        repository.getAllProduct().collect{
            emit(it)
        }
    }
}