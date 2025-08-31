package com.passioagogo.market.domain.bean

data class SubcategoriaImport(
    val id: Long = 0L,
    val nombre: String,
    val descripcion: String?,
    val categoriaNombre: String?, // Para resolver relación después
    val activo: Boolean = true,
    val erroresValidacion: MutableList<String> = mutableListOf()
) {
    val esValida: Boolean
        get() = erroresValidacion.isEmpty()

    fun toSubcategoria(categoriaId: Long): Subcategoria {
        return Subcategoria(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            categoriaId = categoriaId,
            activo = activo
        )
    }
}