package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamiliaDto(
    @SerialName("id")
    val id: String,  // UUID de Supabase

    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,  // ISO 8601

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateFamiliaDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String,

    @SerialName("activo")
    val activo: Boolean = true
)