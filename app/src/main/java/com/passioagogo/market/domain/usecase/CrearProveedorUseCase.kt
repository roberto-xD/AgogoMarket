package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.repository.IProveedorRepository
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject
import javax.inject.Singleton

data class CrearProveedorParams(
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?
)

@Singleton
class CrearProveedorUseCase @Inject constructor(
    private val proveedorRepository: IProveedorRepository
) : UseCase<CrearProveedorParams, Long>() {

    override suspend fun execute(parameters: CrearProveedorParams): Long {
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