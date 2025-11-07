package com.passioagogo.market.domain.usecase.familias

import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.repository.FamiliaRepository
import com.passioagogo.market.domain.usecase.base.NoParamsUseCase
import jakarta.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ObtenerFamiliasUseCase @Inject constructor(
    private val familiaRepository: FamiliaRepository
) : NoParamsUseCase<Flow<List<Familia>>>(){
    override suspend fun execute(): Flow<List<Familia>> {
        return familiaRepository.obtenerTodasLasFamilias()
    }
}