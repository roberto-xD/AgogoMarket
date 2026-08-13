package com.passioagogo.market.data.guides

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ Dominio ============

data class Guide(
    val id: String,
    val titulo: String,
    val resumen: String?,
    val emoji: String?,
    val uso: String?,
    val limpieza: String?,
    val cuidados: String?,
    /** Se pinta destacada en la web. */
    val advertencias: String?,
    val categoryId: String?,
    val orden: Int,
    val activo: Boolean,
) {
    val etiqueta: String get() = listOfNotNull(emoji, titulo).joinToString(" ")

    /** El servidor exige al menos uno de los tres bloques principales. */
    val tieneContenido: Boolean
        get() = !uso.isNullOrBlank() || !limpieza.isNullOrBlank() || !cuidados.isNullOrBlank()
}

data class GuideDraft(
    val titulo: String,
    val resumen: String?,
    val emoji: String?,
    val uso: String?,
    val limpieza: String?,
    val cuidados: String?,
    val advertencias: String?,
    val categoryId: String?,
    val orden: Int,
)

// ============ DTOs ============

@Serializable
data class GuideDto(
    val id: String,
    val titulo: String,
    val resumen: String? = null,
    val emoji: String? = null,
    val uso: String? = null,
    val limpieza: String? = null,
    val cuidados: String? = null,
    val advertencias: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true,
) {
    fun toDomain() = Guide(
        id = id, titulo = titulo, resumen = resumen, emoji = emoji,
        uso = uso, limpieza = limpieza, cuidados = cuidados,
        advertencias = advertencias, categoryId = categoryId,
        orden = orden, activo = activo,
    )
}

@Serializable
data class NewGuideDto(
    val titulo: String,
    val resumen: String? = null,
    val emoji: String? = null,
    val uso: String? = null,
    val limpieza: String? = null,
    val cuidados: String? = null,
    val advertencias: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val orden: Int = 0,
)

// ============ Repositorio ============

interface GuideRepository {
    suspend fun getGuides(): DataResult<List<Guide>>
    suspend fun createGuide(draft: GuideDraft): DataResult<Guide>
    suspend fun updateGuide(guide: Guide): DataResult<Guide>
    suspend fun deleteGuide(id: String): DataResult<Unit>
}

@Singleton
class GuideRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : GuideRepository {

    private companion object { const val TABLE = "guides" }

    override suspend fun getGuides(): DataResult<List<Guide>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).select {
                order("orden", Order.ASCENDING)
                order("titulo", Order.ASCENDING)
            }.decodeList<GuideDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createGuide(draft: GuideDraft): DataResult<Guide> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).insert(
                NewGuideDto(
                    titulo = draft.titulo,
                    resumen = draft.resumen,
                    emoji = draft.emoji,
                    uso = draft.uso,
                    limpieza = draft.limpieza,
                    cuidados = draft.cuidados,
                    advertencias = draft.advertencias,
                    categoryId = draft.categoryId,
                    orden = draft.orden,
                )
            ) { select() }.decodeSingle<GuideDto>()
        }.map { it.toDomain() }
    }

    override suspend fun updateGuide(guide: Guide): DataResult<Guide> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).update({
                set("titulo", guide.titulo)
                set("resumen", guide.resumen)
                set("emoji", guide.emoji)
                set("uso", guide.uso)
                set("limpieza", guide.limpieza)
                set("cuidados", guide.cuidados)
                set("advertencias", guide.advertencias)
                set("category_id", guide.categoryId)
                set("orden", guide.orden)
                set("activo", guide.activo)
            }) {
                select()
                filter { eq("id", guide.id) }
            }.decodeSingle<GuideDto>()
        }.map { it.toDomain() }
    }

    override suspend fun deleteGuide(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).delete { filter { eq("id", id) } }
            Unit
        }
    }
}
