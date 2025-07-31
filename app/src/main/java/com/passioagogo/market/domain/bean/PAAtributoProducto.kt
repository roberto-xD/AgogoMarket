package com.passioagogo.market.domain.bean

data class AtributoProducto(
    val id: Long = 0,
    val nombre: String,
    val valor: String,
    val tipoDato: TipoDatoAtributo = TipoDatoAtributo.TEXT
)

enum class TipoDatoAtributo {
    TEXT, NUMBER, BOOLEAN, DATE
}