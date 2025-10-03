package com.passioagogo.market.domain.bean

import com.passioagogo.market.data.local.entity.base.FamiliaEntity

data class Familia(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: FamiliaEntity): Familia {
            return Familia(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                activo = entity.activo,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    fun toEntity(): FamiliaEntity {
        return FamiliaEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            activo = activo,
            fechaCreacion = fechaCreacion
        )
    }
}
