package com.passioagogo.market.data.local.entity.utils

data class ProductoAtributoConTipo(
    val id: Long,
    val productoId: Long,
    val tipoAtributoId: Long,
    val valor: String,
    val nombreAtributo: String,
    val tipoDato: String
)
