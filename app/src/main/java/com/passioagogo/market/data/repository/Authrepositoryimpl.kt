package com.passioagogo.market.data.repository

import android.util.Log
import com.passioagogo.market.domain.model.auth.AuthResult
import com.passioagogo.market.domain.model.auth.AuthState
import com.passioagogo.market.domain.model.auth.User
import com.passioagogo.market.domain.model.auth.UserRole
import com.passioagogo.market.domain.repository.AuthRepository
import com.passioagogo.market.data.remote.dto.UserProfileDto
import com.passioagogo.market.data.remote.dto.toDomain
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val authState: Flow<AuthState> = supabaseClient.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = getCurrentUser()
                if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Error("No se pudo obtener el perfil del usuario")
                }
            }
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.LoadingFromStorage -> AuthState.Loading
            is SessionStatus.NetworkError -> AuthState.Error("Error de conexión: ${status}")
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = getCurrentUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("No se pudo obtener el perfil del usuario")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e), e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val authUser = supabaseClient.auth.currentUserOrNull()
            if (authUser != null) {
                // Verificar si el perfil existe, si no, crearlo
                ensureUserProfileExists(authUser.id, authUser.email ?: "")
                val user = getCurrentUser()
                if (user != null) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("No se pudo obtener el perfil del usuario")
                }
            } else {
                AuthResult.Error("Error al autenticar con Google")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e), e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): AuthResult {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val authUser = supabaseClient.auth.currentUserOrNull()
            if (authUser != null) {
                // Crear perfil del usuario
                createUserProfile(authUser.id, email, displayName)
                val user = getCurrentUser()
                if (user != null) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("Cuenta creada. Por favor verifica tu correo electrónico.")
                }
            } else {
                // Supabase puede requerir verificación de email
                AuthResult.Error("Cuenta creada. Por favor verifica tu correo electrónico.")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e), e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val authUser = supabaseClient.auth.currentUserOrNull() ?: return null

            val profile = supabaseClient.postgrest
                .from("user_profiles")
                .select()
                .decodeSingleOrNull<UserProfileDto>()

            profile?.toDomain() ?: User(
                id = authUser.id,
                email = authUser.email ?: "",
                displayName = null,
                avatarUrl = null,
                role = UserRole.USER,
                createdAt = System.currentTimeMillis(),
                lastSignInAt = null
            )
        } catch (e: Exception) {
            Log.i(TAG_PG,"error login: $e")
            null
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        return try {
            supabaseClient.postgrest
                .from("user_profiles")
                .update(mapOf("role" to role)) {
                    filter { eq("id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createUserProfile(userId: String, email: String, displayName: String?) {
        try {
            supabaseClient.postgrest
                .from("user_profiles")
                .insert(
                    mapOf(
                        "id" to userId,
                        "email" to email,
                        "display_name" to displayName,
                        "role" to "user"
                    )
                )
        } catch (e: Exception) {
            // El perfil puede ya existir si el usuario se registró con OAuth primero
        }
    }

    private suspend fun ensureUserProfileExists(userId: String, email: String) {
        try {
            val exists = supabaseClient.postgrest
                .from("user_profiles")
                .select()
                .decodeSingleOrNull<UserProfileDto>()

            if (exists == null) {
                createUserProfile(userId, email, null)
            }
        } catch (e: Exception) {
            createUserProfile(userId, email, null)
        }
    }

    private fun mapAuthError(e: Exception): String {
        val message = e.message ?: "Error desconocido"
        return when {
            message.contains("Invalid login credentials") -> "Credenciales inválidas"
            message.contains("Email not confirmed") -> "Por favor verifica tu correo electrónico"
            message.contains("User already registered") -> "Este correo ya está registrado"
            message.contains("Password should be at least") -> "La contraseña debe tener al menos 6 caracteres"
            message.contains("Unable to validate email") -> "El correo electrónico no es válido"
            message.contains("Network") || message.contains("timeout") -> "Error de conexión. Verifica tu internet."
            else -> message
        }
    }
}