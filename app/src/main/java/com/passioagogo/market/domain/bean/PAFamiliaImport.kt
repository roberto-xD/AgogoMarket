package com.passioagogo.market.domain.bean

data class FamiliaImport(
    val id: Long = 0L,
    val nombre: String,
    val descripcion: String?,
    val activo: Boolean = true,
    val erroresValidacion: MutableList<String> = mutableListOf()
) {
    val esValida: Boolean
        get() = erroresValidacion.isEmpty()

    fun toFamilia(): Familia {
        return Familia(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            activo = activo
        )
    }
}