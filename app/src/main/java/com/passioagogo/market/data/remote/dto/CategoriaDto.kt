package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoriaDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String?,

    @SerialName("familia_id")
    val familiaId: String,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateCategoriaDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String?,

    @SerialName("familia_id")
    val familiaId: String,

    @SerialName("activo")
    val activo: Boolean = true
)