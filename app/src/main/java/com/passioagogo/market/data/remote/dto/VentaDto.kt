package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VentaDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("cliente_nombre")
    val clienteNombre: String,

    @SerialName("cliente_telefono")
    val clienteTelefono: String,

    @SerialName("cliente_direccion")
    val clienteDireccion: String,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,

    @SerialName("fecha_liquidacion")
    val fechaLiquidacion: String?,

    @SerialName("total")
    val total: Double,

    @SerialName("metodo_pago")
    val metodoPago: String,

    @SerialName("instrucciones_entrega")
    val instruccionesEntrega: String,

    @SerialName("estado")
    val estado: String
)

@Serializable
data class CreateVentaDto(
    @SerialName("user_id")
    val userId: String,

    @SerialName("cliente_nombre")
    val clienteNombre: String = "Público en general",

    @SerialName("cliente_telefono")
    val clienteTelefono: String = "",

    @SerialName("cliente_direccion")
    val clienteDireccion: String = "",

    @SerialName("total")
    val total: Double = 0.0,

    @SerialName("metodo_pago")
    val metodoPago: String = "",

    @SerialName("instrucciones_entrega")
    val instruccionesEntrega: String = "",

    @SerialName("estado")
    val estado: String = "PENDIENTE"
)
