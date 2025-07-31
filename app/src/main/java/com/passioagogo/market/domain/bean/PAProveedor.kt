package com.passioagogo.market.domain.bean

import com.passioagogo.market.data.local.entity.base.ProveedorEntity

data class Proveedor(
    val id: Long = 0,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: ProveedorEntity): Proveedor {
            return Proveedor(
                id = entity.id,
                nombre = entity.nombre,
                contacto = entity.contacto,
                telefono = entity.telefono,
                email = entity.email,
                direccion = entity.direccion,
                activo = entity.activo,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    fun toEntity(): ProveedorEntity {
        return ProveedorEntity(
            id = id,
            nombre = nombre,
            contacto = contacto,
            telefono = telefono,
            email = email,
            direccion = direccion,
            activo = activo,
            fechaCreacion = fechaCreacion
        )
    }
}