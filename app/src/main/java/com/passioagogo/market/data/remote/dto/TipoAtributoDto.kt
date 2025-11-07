package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TipoAtributoDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("tipo_dato")
    val tipoDato: String,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CreateTipoAtributoDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("tipo_dato")
    val tipoDato: String = "TEXT",

    @SerialName("activo")
    val activo: Boolean = true
)