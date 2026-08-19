package com.passioagogo.market.core.push

import com.google.firebase.messaging.FirebaseMessaging
import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDeviceDto(
    @SerialName("profile_id") val profileId: String,
    @SerialName("fcm_token") val fcmToken: String,
    val plataforma: String = "android",
)

/**
 * Registra el token FCM del dispositivo contra el perfil autenticado.
 * La Edge Function `notify-admins` lee esta tabla para saber a quién
 * notificar.
 */
@Singleton
class PushTokenRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private companion object { const val TABLE = "user_devices" }

    /** Pide el token actual y lo registra. Se llama al iniciar sesión. */
    suspend fun sincronizar(): DataResult<Unit> {
        val token = obtenerToken() ?: return DataResult.Success(Unit)
        return registrarToken(token)
    }

    suspend fun registrarToken(token: String): DataResult<Unit> = withContext(io) {
        val userId = auth.currentUserOrNull()?.id
            ?: return@withContext DataResult.Success(Unit) // sin sesión aún
        safeSupabaseCall {
            // onConflict sobre el token: si el dispositivo cambia de usuario
            // (teléfono compartido), la fila se reasigna en vez de duplicarse.
            postgrest.from(TABLE).upsert(
                UserDeviceDto(profileId = userId, fcmToken = token),
            ) { onConflict = "fcm_token" }
            Unit
        }
    }

    /**
     * Borra el token al cerrar sesión: si no, quien inicie sesión después
     * en este teléfono seguiría recibiendo alertas del usuario anterior.
     */
    suspend fun eliminarTokenActual(): DataResult<Unit> = withContext(io) {
        val token = obtenerToken() ?: return@withContext DataResult.Success(Unit)
        safeSupabaseCall {
            postgrest.from(TABLE).delete { filter { eq("fcm_token", token) } }
            Unit
        }
    }

    private suspend fun obtenerToken(): String? = suspendCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { tarea ->
                cont.resume(if (tarea.isSuccessful) tarea.result else null)
            }
    }
}
