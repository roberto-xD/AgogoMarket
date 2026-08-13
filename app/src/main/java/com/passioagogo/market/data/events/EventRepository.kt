package com.passioagogo.market.data.events

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ Dominio ============

data class Event(
    val id: String,
    val titulo: String,
    val resumen: String?,
    val detalles: String?,
    val lugar: String?,
    /** Ruta relativa al bucket público o URL absoluta. */
    val imagen: String?,
    /** ISO-8601 UTC. */
    val fechaInicio: String,
    /** null = evento de un solo momento. */
    val fechaFin: String?,
    val enlace: String?,
    val orden: Int,
    val activo: Boolean,
    /** Generada por la BD: coalesce(fecha_fin, fecha_inicio). */
    val vigenteHasta: String?,
)

data class EventDraft(
    val titulo: String,
    val resumen: String?,
    val detalles: String?,
    val lugar: String?,
    val imagen: String?,
    val fechaInicio: String,
    val fechaFin: String?,
    val enlace: String?,
    val orden: Int,
)

// ============ DTOs ============

@Serializable
data class EventDto(
    val id: String,
    val titulo: String,
    val resumen: String? = null,
    val detalles: String? = null,
    val lugar: String? = null,
    val imagen: String? = null,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    val enlace: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true,
    @SerialName("vigente_hasta") val vigenteHasta: String? = null,
) {
    fun toDomain() = Event(
        id = id, titulo = titulo, resumen = resumen, detalles = detalles,
        lugar = lugar, imagen = imagen, fechaInicio = fechaInicio, fechaFin = fechaFin,
        enlace = enlace, orden = orden, activo = activo, vigenteHasta = vigenteHasta,
    )
}

/** Sin `vigente_hasta`: es columna generada y el servidor la rechaza. */
@Serializable
data class NewEventDto(
    val titulo: String,
    val resumen: String? = null,
    val detalles: String? = null,
    val lugar: String? = null,
    val imagen: String? = null,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    val enlace: String? = null,
    val orden: Int = 0,
)

@Serializable
data class SiteSettingDto(
    val clave: String,
    val valor: String,
)

// ============ Repositorio ============

interface EventRepository {
    suspend fun getEvents(): DataResult<List<Event>>
    suspend fun createEvent(draft: EventDraft): DataResult<Event>
    suspend fun updateEvent(event: Event): DataResult<Event>
    suspend fun deleteEvent(id: String): DataResult<Unit>

    suspend fun uploadImage(jpegBytes: ByteArray): DataResult<String>
    fun resolveImageUrl(imagen: String): String

    /** Interruptor del widget flotante de la web. */
    suspend fun isWidgetVisible(): DataResult<Boolean>
    suspend fun setWidgetVisible(visible: Boolean): DataResult<Unit>
}

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
    @IoDispatcher private val io: CoroutineDispatcher,
) : EventRepository {

    private companion object {
        const val TABLE = "events"
        const val SETTINGS = "site_settings"
        const val CLAVE_WIDGET = "widget_eventos_visible"
        const val BUCKET = "inventory"
        const val FOLDER = "eventos"
    }

    override suspend fun getEvents(): DataResult<List<Event>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).select {
                order("fecha_inicio", Order.ASCENDING)
                order("orden", Order.ASCENDING)
            }.decodeList<EventDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createEvent(draft: EventDraft): DataResult<Event> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).insert(
                NewEventDto(
                    titulo = draft.titulo,
                    resumen = draft.resumen,
                    detalles = draft.detalles,
                    lugar = draft.lugar,
                    imagen = draft.imagen,
                    fechaInicio = draft.fechaInicio,
                    fechaFin = draft.fechaFin,
                    enlace = draft.enlace,
                    orden = draft.orden,
                )
            ) { select() }.decodeSingle<EventDto>()
        }.map { it.toDomain() }
    }

    override suspend fun updateEvent(event: Event): DataResult<Event> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).update({
                set("titulo", event.titulo)
                set("resumen", event.resumen)
                set("detalles", event.detalles)
                set("lugar", event.lugar)
                set("imagen", event.imagen)
                set("fecha_inicio", event.fechaInicio)
                set("fecha_fin", event.fechaFin)
                set("enlace", event.enlace)
                set("orden", event.orden)
                set("activo", event.activo)
            }) {
                select()
                filter { eq("id", event.id) }
            }.decodeSingle<EventDto>()
        }.map { it.toDomain() }
    }

    override suspend fun deleteEvent(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).delete { filter { eq("id", id) } }
            Unit
        }
    }

    override suspend fun uploadImage(jpegBytes: ByteArray): DataResult<String> = withContext(io) {
        safeSupabaseCall {
            val path = "$FOLDER/${UUID.randomUUID()}.jpg"
            storage.from(BUCKET).upload(path, jpegBytes)
            path
        }
    }

    override fun resolveImageUrl(imagen: String): String =
        if (imagen.startsWith("http://") || imagen.startsWith("https://")) imagen
        else storage.from(BUCKET).publicUrl(imagen)

    override suspend fun isWidgetVisible(): DataResult<Boolean> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(SETTINGS).select {
                filter { eq("clave", CLAVE_WIDGET) }
            }.decodeList<SiteSettingDto>()
                .firstOrNull()?.valor?.equals("true", ignoreCase = true) ?: false
        }
    }

    override suspend fun setWidgetVisible(visible: Boolean): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(SETTINGS).update({
                set("valor", if (visible) "true" else "false")
            }) { filter { eq("clave", CLAVE_WIDGET) } }
            Unit
        }
    }
}
