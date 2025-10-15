package com.passioagogo.market.presentation.view.models

data class PAFamiliasModel(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
)

fun List<PAFamiliasModel>.getDescripcion( id: Long): String{
    if(id == 0L) return ""
    if(this.isEmpty()) return ""
    return this.find { it.id == id }?.descripcion.orEmpty()
}

