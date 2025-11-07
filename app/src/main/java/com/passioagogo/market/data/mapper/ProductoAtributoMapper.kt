package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.dinamics.ProductoAtributoEntity
import com.passioagogo.market.data.remote.dto.CreateProductoAtributoDto
import com.passioagogo.market.data.remote.dto.ProductoAtributoDto
import java.util.UUID

fun ProductoAtributoDto.toEntity(
    localId: Long? = null,
    localProductoId: Long,
    localTipoAtributoId: Long
): ProductoAtributoEntity {
    return ProductoAtributoEntity(
        id = localId ?: 0,
        remoteId = id,
        productoId = localProductoId,
        tipoAtributoId = localTipoAtributoId,
        productoRemoteId = productoId,
        tipoAtributoRemoteId = tipoAtributoId,
        valor = valor,
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun ProductoAtributoEntity.toCreateDto(): CreateProductoAtributoDto {
    return CreateProductoAtributoDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        tipoAtributoId = tipoAtributoRemoteId ?: throw IllegalStateException("tipoAtributoRemoteId no puede ser null"),
        valor = valor
    )
}

fun ProductoAtributoEntity.toDto(): ProductoAtributoDto {
    return ProductoAtributoDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        productoId = productoRemoteId ?: "",
        tipoAtributoId = tipoAtributoRemoteId ?: "",
        valor = valor,
        createdAt = DateMapper.formatToIsoNonNull(System.currentTimeMillis())
    )
}