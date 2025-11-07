package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.remote.dto.CreateProductoDto
import com.passioagogo.market.data.remote.dto.ProductoDto
import java.util.UUID

fun ProductoDto.toEntity(
    localId: Long? = null,
    localProveedorPrincipalId: Long? = null
): ProductoEntity {
    return ProductoEntity(
        id = localId ?: 0,
        remoteId = id,
        userId = userId,
        nombre = nombre,
        descripcion = descripcion,
        codigoBarras = codigoBarras,
        skuInterno = skuInterno,
        precioCompra = precioCompra,
        precioVenta = precioVenta,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        cantidadMaximaComprada = cantidadMaximaComprada,
        proveedorPrincipalId = localProveedorPrincipalId,
        proveedorPrincipalRemoteId = proveedorPrincipalId,
        color = color,
        fechaUltimaVenta = DateMapper.parseIsoDate(fechaUltimaVenta),
        fechaUltimaCompra = DateMapper.parseIsoDate(fechaUltimaCompra),
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        fechaActualizacion = DateMapper.parseIsoDate(fechaActualizacion) ?: System.currentTimeMillis(),
        updatedAt = DateMapper.parseIsoDate(updatedAt),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun ProductoEntity.toCreateDto(): CreateProductoDto {
    return CreateProductoDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        nombre = nombre,
        descripcion = descripcion,
        codigoBarras = codigoBarras,
        skuInterno = skuInterno,
        precioCompra = precioCompra,
        precioVenta = precioVenta,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        cantidadMaximaComprada = cantidadMaximaComprada,
        proveedorPrincipalId = proveedorPrincipalRemoteId,
        color = color,
        activo = activo
    )
}

fun ProductoEntity.toDto(): ProductoDto {
    return ProductoDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        userId = userId ?: "",
        nombre = nombre,
        descripcion = descripcion,
        codigoBarras = codigoBarras,
        skuInterno = skuInterno,
        precioCompra = precioCompra,
        precioVenta = precioVenta,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        cantidadMaximaComprada = cantidadMaximaComprada,
        proveedorPrincipalId = proveedorPrincipalRemoteId,
        color = color,
        fechaUltimaVenta = DateMapper.formatToIso(fechaUltimaVenta),
        fechaUltimaCompra = DateMapper.formatToIso(fechaUltimaCompra),
        activo = activo,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
        fechaActualizacion = DateMapper.formatToIsoNonNull(fechaActualizacion),
        updatedAt = DateMapper.formatToIso(updatedAt)
    )
}