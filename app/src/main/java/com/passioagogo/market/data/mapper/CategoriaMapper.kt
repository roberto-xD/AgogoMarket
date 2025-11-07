package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import com.passioagogo.market.data.remote.dto.CategoriaDto
import com.passioagogo.market.data.remote.dto.CreateCategoriaDto
import java.util.UUID

fun CategoriaDto.toEntity(
    localId: Long? = null,
    localFamiliaId: Long
): CategoriaEntity {
    return CategoriaEntity(
        id = localId ?: 0,
        remoteId = id,
        userId = userId,
        nombre = nombre,
        descripcion = descripcion,
        familiaId = localFamiliaId,  // FK local
        familiaRemoteId = familiaId,  // FK remota
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        updatedAt = DateMapper.parseIsoDate(updatedAt),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun CategoriaEntity.toCreateDto(): CreateCategoriaDto {
    return CreateCategoriaDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        nombre = nombre,
        descripcion = descripcion,
        familiaId = familiaRemoteId ?: throw IllegalStateException("familiaRemoteId no puede ser null"),
        activo = activo
    )
}

fun CategoriaEntity.toDto(): CategoriaDto {
    return CategoriaDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        userId = userId ?: "",
        nombre = nombre,
        descripcion = descripcion,
        familiaId = familiaRemoteId ?: "",
        activo = activo,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
        updatedAt = DateMapper.formatToIso(updatedAt)
    )
}