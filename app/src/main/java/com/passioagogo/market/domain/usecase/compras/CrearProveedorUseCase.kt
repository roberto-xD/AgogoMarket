package com.passioagogo.market.domain.usecase.compras

import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.repository.IProveedorRepository
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import com.passioagogo.market.presentation.view.components.ProveedorModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrearProveedorUseCase @Inject constructor(
    private val proveedorRepository: IProveedorRepository
) : UseCase<ProveedorModel, Long>() {

    override suspend fun execute(parameters: ProveedorModel): Long {
        if (parameters.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del proveedor no puede estar vacío")
        }

        val proveedor = Proveedor(
            nombre = parameters.nombre,
            contacto = parameters.contacto,
            telefono = parameters.telefono,
            email = parameters.email,
            direccion = parameters.direccion
        )

        return when (val result = proveedorRepository.guardarProveedor(proveedor)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}