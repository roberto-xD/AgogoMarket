package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProveedorDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("contacto")
    val contacto: String?,

    @SerialName("telefono")
    val telefono: String?,

    @SerialName("email")
    val email: String?,

    @SerialName("direccion")
    val direccion: String?,

    @SerialName("activo")
    val activo: Boolean,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class CreateProveedorDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("contacto")
    val contacto: String? = null,

    @SerialName("telefono")
    val telefono: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("direccion")
    val direccion: String? = null,

    @SerialName("activo")
    val activo: Boolean = true
)