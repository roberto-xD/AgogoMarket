package com.passioagogo.market.data.contact

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

/**
 * Mensajes del formulario de contacto de la web (tabla contact_messages).
 * Solo lectura desde la app: los mensajes entran por la Edge Function
 * `contact` con service_role, y la tabla no expone política de INSERT.
 */

data class ContactMessage(
    val id: String,
    val nombre: String,
    val email: String,
    val mensaje: String,
    val atendido: Boolean,
    val createdAt: String?,
)

@Serializable
data class ContactMessageDto(
    val id: String,
    val nombre: String,
    val email: String,
    val mensaje: String,
    val atendido: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = ContactMessage(
        id = id, nombre = nombre, email = email,
        mensaje = mensaje, atendido = atendido, createdAt = createdAt,
    )
}

interface ContactRepository {
    suspend fun getMessages(pendingOnly: Boolean = false): DataResult<List<ContactMessage>>
}

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ContactRepository {

    private companion object { const val TABLE = "contact_messages" }

    override suspend fun getMessages(pendingOnly: Boolean): DataResult<List<ContactMessage>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).select {
                    if (pendingOnly) filter { eq("atendido", false) }
                    order("created_at", Order.DESCENDING)
                    limit(200)
                }.decodeList<ContactMessageDto>()
            }.map { list -> list.map { it.toDomain() } }
        }
}
