package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.dinamics.TipoAtributoEntity
import com.passioagogo.market.data.remote.dto.CreateTipoAtributoDto
import com.passioagogo.market.data.remote.dto.TipoAtributoDto
import java.util.UUID

fun TipoAtributoDto.toEntity(localId: Long? = null): TipoAtributoEntity {
    return TipoAtributoEntity(
        id = localId ?: 0,
        remoteId = id,
        userId = userId,
        nombre = nombre,
        tipoDato = tipoDato,
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(createdAt) ?: System.currentTimeMillis(),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

fun TipoAtributoEntity.toCreateDto(): CreateTipoAtributoDto {
    return CreateTipoAtributoDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        nombre = nombre,
        tipoDato = tipoDato,
        activo = activo
    )
}

fun TipoAtributoEntity.toDto(): TipoAtributoDto {
    return TipoAtributoDto(
        id = remoteId ?: UUID.randomUUID().toString(),
        userId = userId ?: "",
        nombre = nombre,
        tipoDato = tipoDato,
        activo = activo,
        createdAt = DateMapper.formatToIsoNonNull(fechaCreacion)
    )
}