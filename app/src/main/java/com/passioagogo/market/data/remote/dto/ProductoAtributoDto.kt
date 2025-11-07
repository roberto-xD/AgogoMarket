package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoAtributoDto(
    @SerialName("id")
    val id: String,

    @SerialName("producto_id")
    val productoId: String,

    @SerialName("tipo_atributo_id")
    val tipoAtributoId: String,

    @SerialName("valor")
    val valor: String,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CreateProductoAtributoDto(
    @SerialName("producto_id")
    val productoId: String,

    @SerialName("tipo_atributo_id")
    val tipoAtributoId: String,

    @SerialName("valor")
    val valor: String
)