package com.passioagogo.market.domain.usecase

import android.util.Log
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.data.repository.PARepositoryImpl
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.repository.PADBRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PAGetAllProductLocalUseCase @Inject constructor(
    private val repository: PADBRepository
){
    operator fun invoke(): Flow<List<PAProductEntity>>{
        Log.i(TAG_PG,"on get use case")
        return repository.getAllProduct()
    }
}