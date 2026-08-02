package com.passioagogo.market.data.auth

import com.passioagogo.market.core.di.ApplicationScope
import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.Profile
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.common.UserRole
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val nombre: String,
    val rol: UserRole,
    @SerialName("location_id") val locationId: String? = null,
    val activo: Boolean = true,
) {
    fun toDomain() = Profile(
        id = id, nombre = nombre, rol = rol,
        locationId = locationId, activo = activo,
    )
}

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    @ApplicationScope scope: CoroutineScope,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AuthRepository {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Initializing)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /** Evita recargar el profile en cada refresh de token del mismo usuario. */
    private var loadedUserId: String? = null

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Initializing ->
                        _sessionState.value = SessionState.Initializing

                    is SessionStatus.NotAuthenticated -> {
                        loadedUserId = null
                        _sessionState.value = SessionState.NotAuthenticated
                    }

                    is SessionStatus.Authenticated -> {
                        val userId = status.session.user?.id
                            ?: auth.currentUserOrNull()?.id
                        when {
                            userId == null ->
                                _sessionState.value =
                                    SessionState.Error(DataError.Unknown("Sesión sin usuario"))
                            // Mismo usuario ya cargado (p. ej. token refresh): no refetch
                            userId == loadedUserId &&
                                _sessionState.value is SessionState.Authenticated -> Unit
                            else -> loadProfile(userId)
                        }
                    }

                    is SessionStatus.RefreshFailure ->
                        _sessionState.value = SessionState.Error(DataError.Network)
                }
            }
        }
    }

    private suspend fun loadProfile(userId: String) {
        val result = withContext(io) {
            safeSupabaseCall {
                postgrest.from("profiles").select {
                    filter { eq("id", userId) }
                }.decodeSingle<ProfileDto>()
            }
        }
        _sessionState.value = when (result) {
            is DataResult.Error -> SessionState.Error(result.error)
            is DataResult.Success -> {
                val profile = result.data.toDomain()
                if (!profile.activo) {
                    SessionState.Inactive
                } else {
                    loadedUserId = profile.id
                    SessionState.Authenticated(profile)
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String): DataResult<Unit> =
        withContext(io) {
            try {
                auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
                // El collector de sessionStatus cargará el profile
                DataResult.Success(Unit)
            } catch (e: AuthRestException) {
                DataResult.Error(e.toDataError())
            } catch (e: Exception) {
                DataResult.Error(DataError.Network)
            }
        }

    override suspend fun signOut(): DataResult<Unit> = withContext(io) {
        safeSupabaseCall { auth.signOut() }.map { }
    }

    override suspend fun refreshProfile(): DataResult<Unit> {
        val userId = auth.currentUserOrNull()?.id
            ?: return DataResult.Error(DataError.Unauthorized)
        loadedUserId = null
        loadProfile(userId)
        return when (val state = _sessionState.value) {
            is SessionState.Error -> DataResult.Error(state.error)
            else -> DataResult.Success(Unit)
        }
    }

    /** Mensajes de auth traducidos para la UI. */
    private fun AuthRestException.toDataError(): DataError = when (errorCode?.value) {
        "invalid_credentials" -> DataError.Business("Correo o contraseña incorrectos")
        "email_not_confirmed" -> DataError.Business("Confirma tu correo antes de iniciar sesión")
        "user_banned" -> DataError.Business("Esta cuenta está suspendida")
        "over_request_rate_limit" ->
            DataError.Business("Demasiados intentos, espera un momento")
        else -> DataError.Business(message ?: "No se pudo iniciar sesión")
    }
}
