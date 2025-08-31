package com.passioagogo.market.domain.bean

data class CategoriaImport(
    val id: Long = 0L,
    val nombre: String,
    val descripcion: String?,
    val familiaNombre: String?, // Para resolver relación después
    val activo: Boolean = true,
    val erroresValidacion: MutableList<String> = mutableListOf()
) {
    val esValida: Boolean
        get() = erroresValidacion.isEmpty()

    fun toCategoria(familiaId: Long): Categoria {
        return Categoria(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            familiaId = familiaId,
            activo = activo
        )
    }
}