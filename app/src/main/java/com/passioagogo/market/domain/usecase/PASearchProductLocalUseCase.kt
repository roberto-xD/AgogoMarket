package com.passioagogo.market.domain.usecase

import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.repository.PADBRepository
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class PASearchProductLocalUseCase @Inject constructor(
    private val repository: PADBRepository
){
    suspend operator fun invoke (searchTerm: String) : Flow<PADomainState<List<PAProductEntity>>> = flow{
        val query = repository.searchProductsByName(searchTerm).last()
        emit(
            if(query.isNotEmpty()){
                PADomainState.Success(query)
            }else{
                PADomainState.Error("No data found")
            }
        )
    }
}