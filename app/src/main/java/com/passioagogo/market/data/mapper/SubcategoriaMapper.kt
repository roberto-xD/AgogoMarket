package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import com.passioagogo.market.data.remote.dto.CreateSubcategoriaDto
import com.passioagogo.market.data.remote.dto.SubcategoriaDto
import java.util.UUID

fun SubcategoriaDto.toEntity(
    localId: Long? = null,
    localCategoriaId: Long
): SubcategoriaEntity {
    return SubcategoriaEntity(
        id = localId ?: 0,
        remoteId = id,
        userId = userId,
        nombre = nombre,
        descripcion = descripcion,
        categoriaId = localCategoriaId,
        categoriaRemoteId = categoriaId,
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        updatedAt = DateMapper.parseIsoDate(updatedAt),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun SubcategoriaEntity.toCreateDto(): CreateSubcategoriaDto {
    return CreateSubcategoriaDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        nombre = nombre,
        descripcion = descripcion,
        categoriaId = categoriaRemoteId ?: throw IllegalStateException("categoriaRemoteId no puede ser null"),
        activo = activo
    )
}

fun SubcategoriaEntity.toDto(): SubcategoriaDto {
    return SubcategoriaDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        userId = userId ?: "",
        nombre = nombre,
        descripcion = descripcion,
        categoriaId = categoriaRemoteId ?: "",
        activo = activo,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
        updatedAt = DateMapper.formatToIso(updatedAt)
    )
}