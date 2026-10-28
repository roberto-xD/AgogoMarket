package com.passioagogo.market.data.addresses

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Clave pública de administración registrada en la base. */
data class CryptoKey(
    val id: String,
    val publica: String,
    /** Lo que AdminKeyTool sabe abrir: RSA-OAEP-256. */
    val algoritmo: String,
    val activa: Boolean,
    val notas: String?,
    val createdAt: String?,
) {
    /** Una clave con otro algoritmo no la puede descifrar esta app. */
    val compatible: Boolean get() = algoritmo == ALGORITMO_SOPORTADO
}

/** El par que genera AdminKeyTool: RSA-OAEP con SHA-256. */
const val ALGORITMO_SOPORTADO = "RSA-OAEP-256"

@Serializable
data class CryptoKeyDto(
    val id: String,
    val publica: String,
    val algoritmo: String = ALGORITMO_SOPORTADO,
    val activa: Boolean = true,
    val notas: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = CryptoKey(id, publica, algoritmo, activa, notas, createdAt)
}

@Serializable
data class NewCryptoKeyDto(
    val id: String,
    val publica: String,
    /** La columna es NOT NULL: omitirlo hace fallar el insert. */
    val algoritmo: String = ALGORITMO_SOPORTADO,
    val notas: String? = null,
)

/**
 * Claves públicas con las que el sitio envuelve las direcciones.
 *
 * La privada correspondiente nunca pasa por aquí: vive en el archivo
 * cifrado del administrador.
 */
interface CryptoKeyRepository {
    suspend fun getKeys(): DataResult<List<CryptoKey>>

    /**
     * Registra una pública nueva y desactiva la anterior.
     *
     * Las direcciones ya guardadas siguen envueltas con la clave vieja:
     * no se pueden re-envolver sin descifrarlas una a una.
     */
    suspend fun registrar(
        id: String,
        publica: String,
        notas: String? = null,
    ): DataResult<Unit>

    /** Cuántas direcciones dependen todavía de cada clave. */
    suspend fun contarPorClave(): DataResult<Map<String, Int>>
}

@Singleton
class CryptoKeyRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CryptoKeyRepository {

    private companion object {
        const val TABLE = "crypto_keys"
        const val ADDRESSES = "user_addresses"
    }

    override suspend fun getKeys(): DataResult<List<CryptoKey>> = withContext(io) {
        safeSupabaseCall("getCryptoKeys") {
            postgrest.from(TABLE).select().decodeList<CryptoKeyDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun registrar(
        id: String,
        publica: String,
        notas: String?,
    ): DataResult<Unit> = withContext(io) {
            safeSupabaseCall("registrarCryptoKey") {
                // Primero desactivar: si fallara el insert, quedarse sin
                // clave activa es más seguro que tener dos compitiendo.
                postgrest.from(TABLE).update({ set("activa", false) }) {
                    filter { eq("activa", true) }
                }
                postgrest.from(TABLE).insert(
                    NewCryptoKeyDto(id = id, publica = publica, notas = notas)
                )
                Unit
            }
        }

    override suspend fun contarPorClave(): DataResult<Map<String, Int>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(ADDRESSES).select {
                // Solo la columna de la clave: no hace falta traer los bultos
            }.decodeList<SoloClaveDto>()
                .groupingBy { it.adminKeyId }
                .eachCount()
        }
    }
}

@Serializable
private data class SoloClaveDto(
    @SerialName("admin_key_id") val adminKeyId: String,
)
