package com.passioagogo.market.domain.bean

import com.passioagogo.market.data.local.entity.base.CategoriaEntity

data class Categoria(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String?,
    val familiaId: Long,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: CategoriaEntity): Categoria {
            return Categoria(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                familiaId = entity.familiaId,
                activo = entity.activo,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    fun toEntity(): CategoriaEntity {
        return CategoriaEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            familiaId = familiaId,
            activo = activo,
            fechaCreacion = fechaCreacion
        )
    }
}