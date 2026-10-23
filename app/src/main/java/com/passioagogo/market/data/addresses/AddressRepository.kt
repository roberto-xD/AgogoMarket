package com.passioagogo.market.data.addresses

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
import kotlinx.serialization.json.Json

// ============ Dominio ============

/**
 * Dirección tal como vive en la base: tres bultos cifrados y ninguna
 * clave. Para leerla hace falta la privada de administración.
 */
data class EncryptedAddress(
    val id: String,
    val profileId: String,
    val etiqueta: String?,
    /** El JSON de la dirección, cifrado con una clave de datos al azar. */
    val contenido: String,
    val iv: String,
    /** Esa clave de datos, envuelta con la pública de administración. */
    val dkAdmin: String,
    /** Con qué clave se envolvió; tras una rotación puede no ser la actual. */
    val adminKeyId: String,
    val createdAt: String?,
)

/** La dirección ya descifrada. */
@Serializable
data class DireccionClara(
    val calle: String = "",
    val interior: String = "",
    val colonia: String = "",
    val cp: String = "",
    val ciudad: String = "",
    val estado: String = "",
    val referencias: String = "",
) {
    /** Una línea para copiar o dictar a la paquetería. */
    fun enUnaLinea(): String = listOf(
        listOfNotNull(calle.ifBlank { null }, interior.ifBlank { null })
            .joinToString(" int. "),
        colonia,
        cp,
        ciudad,
        estado,
    ).filter { it.isNotBlank() }.joinToString(", ")
}

// ============ DTOs ============

@Serializable
data class EncryptedAddressDto(
    val id: String,
    @SerialName("profile_id") val profileId: String,
    val etiqueta: String? = null,
    val contenido: String,
    val iv: String,
    @SerialName("dk_admin") val dkAdmin: String,
    @SerialName("admin_key_id") val adminKeyId: String,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = EncryptedAddress(
        id = id, profileId = profileId, etiqueta = etiqueta,
        contenido = contenido, iv = iv, dkAdmin = dkAdmin,
        adminKeyId = adminKeyId, createdAt = createdAt,
    )
}

// ============ Repositorio ============

/**
 * Lectura de las direcciones cifradas.
 *
 * El repositorio NO descifra: solo trae los bultos. Abrirlos es cosa de
 * `AdminKeyStore`, que es quien custodia la clave. Así la capa de datos
 * no tiene forma de ver una dirección en claro ni de filtrarla a un log.
 */
interface AddressRepository {
    suspend fun getDirecciones(profileId: String): DataResult<List<EncryptedAddress>>
}

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AddressRepository {

    private companion object { const val TABLE = "user_addresses" }

    override suspend fun getDirecciones(
        profileId: String,
    ): DataResult<List<EncryptedAddress>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).select {
                filter { eq("profile_id", profileId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<EncryptedAddressDto>()
        }.map { list -> list.map { it.toDomain() } }
    }
}

/** Tolerante: una dirección antigua puede no traer todos los campos. */
val jsonDireccion: Json = Json { ignoreUnknownKeys = true }
