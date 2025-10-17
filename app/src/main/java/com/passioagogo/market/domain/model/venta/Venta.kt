package com.passioagogo.market.domain.model.venta

import com.passioagogo.market.data.local.entity.venta.VentaEntity
import com.passioagogo.market.presentation.view.components.ClienteModel

data class Venta(
    val id: Long = 0,
    val clienteModel: ClienteModel = ClienteModel(),
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaLiquidacion: Long? = null,
    val total: Double = 0.0,
    val metodoPago: String = "",
    val instruccionesEntrega: String = "",
    val estado: EstadoVenta = EstadoVenta.PENDIENTE,
    val productos: List<ProductoVenta> = emptyList()
)

fun Venta.toEntity() = VentaEntity(
    id = id,
    clienteNombre = clienteModel.nombre,
    clienteTelefono = clienteModel.telefono.orEmpty(),
    clienteDireccion = clienteModel.direccion.orEmpty(),
    fechaCreacion = fechaCreacion,
    fechaLiquidacion = fechaLiquidacion,
    total = total,
    metodoPago = metodoPago,
    instruccionesEntrega = instruccionesEntrega,
    estado = estado.name
)