package com.passioagogo.market.data.attributes

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Presets de atributos: pares clave-valor reutilizables que se capturan
 * como chips en lugar de teclear el JSON a mano.
 */
data class AttributePreset(
    val id: String,
    val clave: String,
    val valor: String,
    val identificador: String,
    val emoji: String?,
    val orden: Int,
    val activo: Boolean,
) {
    /** Rótulo de la chip: "🧴 Silicona". */
    val etiqueta: String get() = listOfNotNull(emoji, identificador).joinToString(" ")
}

data class AttributePresetDraft(
    val clave: String,
    val valor: String,
    val identificador: String,
    val emoji: String?,
)

@Serializable
data class AttributePresetDto(
    val id: String,
    val clave: String,
    val valor: String,
    val identificador: String,
    val emoji: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true,
) {
    fun toDomain() = AttributePreset(
        id = id, clave = clave, valor = valor, identificador = identificador,
        emoji = emoji, orden = orden, activo = activo,
    )
}

@Serializable
data class NewAttributePresetDto(
    val clave: String,
    val valor: String,
    val identificador: String,
    val emoji: String? = null,
    val orden: Int = 0,
)

interface AttributePresetRepository {
    suspend fun getPresets(): DataResult<List<AttributePreset>>
    suspend fun createPreset(draft: AttributePresetDraft): DataResult<AttributePreset>
    suspend fun deletePreset(id: String): DataResult<Unit>
}

@Singleton
class AttributePresetRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AttributePresetRepository {

    private companion object { const val TABLE = "attribute_presets" }

    override suspend fun getPresets(): DataResult<List<AttributePreset>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).select {
                filter { eq("activo", true) }
                order("orden", Order.ASCENDING)
                order("created_at", Order.ASCENDING)
            }.decodeList<AttributePresetDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createPreset(
        draft: AttributePresetDraft,
    ): DataResult<AttributePreset> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).insert(
                NewAttributePresetDto(
                    clave = draft.clave.trim().lowercase(),
                    valor = draft.valor.trim(),
                    identificador = draft.identificador.trim(),
                    emoji = draft.emoji?.trim()?.ifBlank { null },
                )
            ) { select() }.decodeSingle<AttributePresetDto>()
        }.map { it.toDomain() }
    }

    override suspend fun deletePreset(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            // Borrado lógico: otros productos pueden seguir usando el par
            postgrest.from(TABLE).update({ set("activo", false) }) {
                filter { eq("id", id) }
            }
            Unit
        }
    }
}
