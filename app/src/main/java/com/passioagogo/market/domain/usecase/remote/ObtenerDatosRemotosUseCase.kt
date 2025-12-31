package com.passioagogo.market.domain.usecase.remote

import com.passioagogo.market.data.remote.dto.FamiliaRemoteDto
import com.passioagogo.market.data.remote.implementation.FamiliaRemoteRepositoryImpl
import com.passioagogo.market.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerDatosRemotosUseCase @Inject constructor(
    private val remoteRepositoryImpl: FamiliaRemoteRepositoryImpl
) : NoParamsUseCase<Unit>(){

    override suspend fun execute(): Unit {
        remoteRepositoryImpl.getAll()
            .onSuccess {

            }
            .onFailure {

            }
    }
}