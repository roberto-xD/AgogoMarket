package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamiliaRemoteDto(
    @SerialName("id")
    val id: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("fechaCreacion")
    val fechaCreacion: String,
)

@Serializable
data class CreateFamiliaDto(

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String,

    @SerialName("activo")
    val activo: Boolean = true
)