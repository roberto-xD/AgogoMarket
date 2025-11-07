package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubcategoriaDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String?,

    @SerialName("categoria_id")
    val categoriaId: String,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateSubcategoriaDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String?,

    @SerialName("categoria_id")
    val categoriaId: String,

    @SerialName("activo")
    val activo: Boolean = true
)