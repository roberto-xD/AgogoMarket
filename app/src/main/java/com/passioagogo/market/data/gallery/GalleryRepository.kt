package com.passioagogo.market.data.gallery

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

data class GalleryItem(
    val id: String,
    val titulo: String,
    val descripcion: String?,
    val detalles: String?,
    /** Ruta relativa al bucket público, o URL absoluta. */
    val imagen: String,
    val categoria: String?,
    val orden: Int,
    val activo: Boolean,
)

data class GalleryDraft(
    val titulo: String,
    val descripcion: String?,
    val detalles: String?,
    val imagen: String,
    val categoria: String?,
    val orden: Int,
)

// ============ DTOs ============

@Serializable
data class GalleryItemDto(
    val id: String,
    val titulo: String,
    val descripcion: String? = null,
    val detalles: String? = null,
    val imagen: String,
    val categoria: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = GalleryItem(
        id = id, titulo = titulo, descripcion = descripcion, detalles = detalles,
        imagen = imagen, categoria = categoria, orden = orden, activo = activo,
    )
}

@Serializable
data class NewGalleryItemDto(
    val titulo: String,
    val descripcion: String? = null,
    val detalles: String? = null,
    val imagen: String,
    val categoria: String? = null,
    val orden: Int = 0,
)

// ============ Repositorio ============

interface GalleryRepository {
    suspend fun getItems(): DataResult<List<GalleryItem>>

    /** Sube la imagen al bucket y devuelve su RUTA RELATIVA (lo que guarda la tabla). */
    suspend fun uploadImage(jpegBytes: ByteArray): DataResult<String>

    suspend fun createItem(draft: GalleryDraft): DataResult<GalleryItem>
    suspend fun updateItem(item: GalleryItem): DataResult<GalleryItem>
    suspend fun deleteItem(id: String): DataResult<Unit>

    /** URL absoluta para pintar la imagen; acepta rutas relativas y URLs. */
    fun resolveImageUrl(imagen: String): String
}

@Singleton
class GalleryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
    @IoDispatcher private val io: CoroutineDispatcher,
) : GalleryRepository {

    private companion object {
        const val TABLE = "gallery_items"

        /**
         * Bucket público que consume la web: las URLs del sitio tienen la forma
         * /storage/v1/object/public/inventory/galeria/…, y la tabla guarda la
         * ruta relativa a este bucket.
         */
        const val BUCKET = "inventory"
        const val FOLDER = "galeria"
    }

    override suspend fun getItems(): DataResult<List<GalleryItem>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).select {
                order("orden", Order.ASCENDING)
                order("created_at", Order.DESCENDING)
            }.decodeList<GalleryItemDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun uploadImage(jpegBytes: ByteArray): DataResult<String> =
        withContext(io) {
            safeSupabaseCall {
                val path = "$FOLDER/${UUID.randomUUID()}.jpg"
                storage.from(BUCKET).upload(path, jpegBytes)
                path // ruta relativa: es lo que espera la web
            }
        }

    override suspend fun createItem(draft: GalleryDraft): DataResult<GalleryItem> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).insert(
                    NewGalleryItemDto(
                        titulo = draft.titulo,
                        descripcion = draft.descripcion,
                        detalles = draft.detalles,
                        imagen = draft.imagen,
                        categoria = draft.categoria,
                        orden = draft.orden,
                    )
                ) { select() }.decodeSingle<GalleryItemDto>()
            }.map { it.toDomain() }
        }

    override suspend fun updateItem(item: GalleryItem): DataResult<GalleryItem> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).update({
                    set("titulo", item.titulo)
                    set("descripcion", item.descripcion)
                    set("detalles", item.detalles)
                    set("imagen", item.imagen)
                    set("categoria", item.categoria)
                    set("orden", item.orden)
                    set("activo", item.activo)
                }) {
                    select()
                    filter { eq("id", item.id) }
                }.decodeSingle<GalleryItemDto>()
            }.map { it.toDomain() }
        }

    override suspend fun deleteItem(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).delete { filter { eq("id", id) } }
            Unit
        }
    }

    override fun resolveImageUrl(imagen: String): String =
        if (imagen.startsWith("http://") || imagen.startsWith("https://")) imagen
        else storage.from(BUCKET).publicUrl(imagen)
}
