package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.utils.HistorialPrecioEntity
import com.passioagogo.market.data.remote.dto.CreateHistorialPrecioDto
import com.passioagogo.market.data.remote.dto.HistorialPrecioDto
import java.util.UUID

fun HistorialPrecioDto.toEntity(
    localId: Long? = null,
    localProductoId: Long
): HistorialPrecioEntity {
    return HistorialPrecioEntity(
        id = localId ?: 0,
        remoteId = id,
        productoId = localProductoId,
        productoRemoteId = productoId,
        precioCompraAnterior = precioCompraAnterior,
        precioVentaAnterior = precioVentaAnterior,
        precioCompraNuevo = precioCompraNuevo,
        precioVentaNuevo = precioVentaNuevo,
        motivo = motivo,
        fechaCambio = DateMapper.parseIsoDate(fechaCambio) ?: System.currentTimeMillis(),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun HistorialPrecioEntity.toCreateDto(): CreateHistorialPrecioDto {
    return CreateHistorialPrecioDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        precioCompraAnterior = precioCompraAnterior,
        precioVentaAnterior = precioVentaAnterior,
        precioCompraNuevo = precioCompraNuevo,
        precioVentaNuevo = precioVentaNuevo,
        motivo = motivo
    )
}

fun HistorialPrecioEntity.toDto(): HistorialPrecioDto {
    return HistorialPrecioDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        productoId = productoRemoteId ?: "",
        precioCompraAnterior = precioCompraAnterior,
        precioVentaAnterior = precioVentaAnterior,
        precioCompraNuevo = precioCompraNuevo,
        precioVentaNuevo = precioVentaNuevo,
        motivo = motivo,
        fechaCambio = DateMapper.formatToIsoNonNull(fechaCambio)
    )
}