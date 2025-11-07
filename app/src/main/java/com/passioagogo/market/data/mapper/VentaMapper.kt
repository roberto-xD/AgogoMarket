package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.venta.VentaEntity
import com.passioagogo.market.data.remote.dto.CreateVentaDto
import com.passioagogo.market.data.remote.dto.VentaDto
import java.util.UUID

fun VentaDto.toEntity(localId: Long? = null): VentaEntity {
    return VentaEntity(
        id = localId ?: 0,
        remoteId = id,
        userId = userId,
        clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono,
        clienteDireccion = clienteDireccion,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        fechaLiquidacion = DateMapper.parseIsoDate(fechaLiquidacion),
        total = total,
        metodoPago = metodoPago,
        instruccionesEntrega = instruccionesEntrega,
        estado = estado,
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun VentaEntity.toCreateDto(): CreateVentaDto {
    return CreateVentaDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono,
        clienteDireccion = clienteDireccion,
        total = total,
        metodoPago = metodoPago,
        instruccionesEntrega = instruccionesEntrega,
        estado = estado
    )
}

fun VentaEntity.toDto(): VentaDto {
    return VentaDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        userId = userId ?: "",
        clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono,
        clienteDireccion = clienteDireccion,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
        fechaLiquidacion = DateMapper.formatToIso(fechaLiquidacion),
        total = total,
        metodoPago = metodoPago,
        instruccionesEntrega = instruccionesEntrega,
        estado = estado
    )
}