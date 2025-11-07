package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import com.passioagogo.market.data.remote.dto.CreateProveedorDto
import com.passioagogo.market.data.remote.dto.ProveedorDto
import java.util.UUID

fun ProveedorDto.toEntity(localId: Long? = null): ProveedorEntity {
    return ProveedorEntity(
        id = localId ?: 0,
        remoteId = id,
        userId = userId,
        nombre = nombre,
        contacto = contacto,
        telefono = telefono,
        email = email,
        direccion = direccion,
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        updatedAt = DateMapper.parseIsoDate(updatedAt),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun ProveedorEntity.toCreateDto(): CreateProveedorDto {
    return CreateProveedorDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        nombre = nombre,
        contacto = contacto,
        telefono = telefono,
        email = email,
        direccion = direccion,
        activo = activo
    )
}

fun ProveedorEntity.toDto(): ProveedorDto {
    return ProveedorDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        userId = userId ?: "",
        nombre = nombre,
        contacto = contacto,
        telefono = telefono,
        email = email,
        direccion = direccion,
        activo = activo,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
        updatedAt = DateMapper.formatToIso(updatedAt)
    )
}