package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.venta.VentaProductoEntity
import com.passioagogo.market.data.remote.dto.CreateVentaProductoDto
import com.passioagogo.market.data.remote.dto.VentaProductoDto
import java.util.UUID

fun VentaProductoDto.toEntity(
    localId: Long? = null,
    localVentaId: Long,
    localProductoId: Long
): VentaProductoEntity {
    return VentaProductoEntity(
        id = localId ?: 0,
        remoteId = id,
        ventaId = localVentaId,
        productoId = localProductoId,
        ventaRemoteId = ventaId,
        productoRemoteId = productoId,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        subtotal = subtotal,
        isSynced = true,
        needsSync = false
    )
}

fun VentaProductoEntity.toCreateDto(): CreateVentaProductoDto {
    return CreateVentaProductoDto(
        ventaId = ventaRemoteId ?: throw IllegalStateException("ventaRemoteId no puede ser null"),
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        subtotal = subtotal
    )
}

fun VentaProductoEntity.toDto(): VentaProductoDto {
    return VentaProductoDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        ventaId = ventaRemoteId ?: "",
        productoId = productoRemoteId ?: "",
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        subtotal = subtotal
    )
}