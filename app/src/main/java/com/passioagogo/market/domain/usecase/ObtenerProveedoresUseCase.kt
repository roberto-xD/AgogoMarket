package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.repository.IProveedorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObtenerProveedoresUseCase @Inject constructor(
    private val proveedorRepository: IProveedorRepository
) : NoParamsUseCase<Flow<List<Proveedor>>>() {

    override suspend fun execute(): Flow<List<Proveedor>> {
        return proveedorRepository.obtenerTodosLosProveedores()
    }
}