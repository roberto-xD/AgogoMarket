package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity
import com.passioagogo.market.data.remote.dto.CreateProductoImagenDto
import com.passioagogo.market.data.remote.dto.ProductoImagenDto
import java.util.UUID

fun ProductoImagenDto.toEntity(
    localId: Long? = null,
    localProductoId: Long
): ProductoImagenEntity {
    return ProductoImagenEntity(
        id = localId ?: 0,
        remoteId = id,
        productoId = localProductoId,
        productoRemoteId = productoId,
        rutaImagen = rutaImagen,
        orden = orden,
        esPrincipal = esPrincipal,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun ProductoImagenEntity.toCreateDto(): CreateProductoImagenDto {
    return CreateProductoImagenDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        rutaImagen = rutaImagen,
        orden = orden,
        esPrincipal = esPrincipal
    )
}

fun ProductoImagenEntity.toDto(): ProductoImagenDto {
    return ProductoImagenDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        productoId = productoRemoteId ?: "",
        rutaImagen = rutaImagen,
        orden = orden,
        esPrincipal = esPrincipal,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion)
    )
}