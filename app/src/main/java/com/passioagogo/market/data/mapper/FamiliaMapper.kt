package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import com.passioagogo.market.data.remote.dto.CreateFamiliaDto
import com.passioagogo.market.data.remote.dto.FamiliaRemoteDto
import java.text.SimpleDateFormat
import java.util.*

// DTO → Entity (cuando viene de Supabase)
fun FamiliaRemoteDto.toEntity(): FamiliaEntity {
    return FamiliaEntity(
        id = 0,
        nombre = nombre,
        descripcion = descripcion,
        activo = activo,
        fechaCreacion = DateMapper.parseIsoDate(fechaCreacion) ?: System.currentTimeMillis(),
    )
}

// Entity → DTO para CREATE (cuando se crea por primera vez)
fun FamiliaEntity.toCreateDto(): CreateFamiliaDto {
    return CreateFamiliaDto(
        nombre = nombre,
        descripcion = descripcion,
        activo = activo
    )
}

// Entity → DTO para UPDATE (ya existe en Supabase)
fun FamiliaEntity.toDto(): FamiliaRemoteDto {
    return FamiliaRemoteDto(
        id = "",  //
        nombre = nombre,
        descripcion = descripcion,
        activo = activo,
        fechaCreacion = DateMapper.formatToIsoNonNull(fechaCreacion),
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