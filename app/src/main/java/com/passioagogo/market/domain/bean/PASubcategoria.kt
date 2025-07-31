package com.passioagogo.market.domain.bean

import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity

data class Subcategoria(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String?,
    val categoriaId: Long,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: SubcategoriaEntity): Subcategoria {
            return Subcategoria(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                categoriaId = entity.categoriaId,
                activo = entity.activo,
                fechaCreacion = entity.fechaCreacion
            )
        }
    }

    fun toEntity(): SubcategoriaEntity {
        return SubcategoriaEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            categoriaId = categoriaId,
            activo = activo,
            fechaCreacion = fechaCreacion
        )
    }
}