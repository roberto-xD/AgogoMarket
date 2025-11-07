package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import com.passioagogo.market.data.remote.dto.CreateFamiliaDto
import com.passioagogo.market.data.remote.dto.FamiliaDto
import java.text.SimpleDateFormat
import java.util.*

// DTO → Entity (cuando viene de Supabase)
fun FamiliaDto.toEntity(localId: Long? = null): FamiliaEntity {
    return FamiliaEntity(
        id = localId ?: 0,  // Si existe local, usar ese ID, sino 0 (Room lo genera)
        remoteId = id,  // UUID de Supabase
        userId = userId,
        nombre = nombre,
        descripcion = descripcion,
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
        updatedAt = DateMapper.parseIsoDate(updatedAt),
        isSynced = true,
        isDeleted = false,
        needsSync = false
    )
}

// Entity → DTO para CREATE (cuando se crea por primera vez)
fun FamiliaEntity.toCreateDto(): CreateFamiliaDto {
    return CreateFamiliaDto(
        userId = userId ?: throw IllegalStateException("userId no puede ser null"),
        nombre = nombre,
        descripcion = descripcion,
        activo = activo
    )
}

// Entity → DTO para UPDATE (ya existe en Supabase)
fun FamiliaEntity.toDto(): FamiliaDto {
    return FamiliaDto(
        id = remoteId ?: UUID.randomUUID().toString(),  // Generar UUID si no existe
        userId = userId ?: "",
        nombre = nombre,
        descripcion = descripcion,
        activo = activo,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
        updatedAt = DateMapper.formatToIso(updatedAt)
    )
}

// Helpers para fechas
private fun parseIsoDate(isoDate: String): Long {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(isoDate)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun formatToIso(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(timestamp))
}