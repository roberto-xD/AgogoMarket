package com.passioagogo.market.domain.usecase

import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PASearchProductLocalUseCase @Inject constructor(
    private val repository: PADBRepository
){
    operator fun invoke (searchTerm: String) : Flow<List<PAProductEntity>>{
        return repository.searchProductsByName(searchTerm)
    }
}