package com.passioagogo.market.domain.auth

import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.UserRole
import kotlinx.coroutines.flow.StateFlow

/** Espejo de public.profiles (02_locations_profiles.sql). */
data class Profile(
    val id: String,
    val nombre: String,
    val rol: UserRole,
    val locationId: String?,
    val activo: Boolean,
)

/**
 * Estado de sesión de la app. Mismo diseño que el cliente web KMP:
 * 5 estados sellados; el vendedor sin tienda NO es un estado aparte
 * sino una propiedad de [Authenticated] (decisión deliberada para no
 * proliferar estados).
 */
sealed interface SessionState {

    /** Cargando la sesión persistida al arrancar. */
    data object Initializing : SessionState

    data object NotAuthenticated : SessionState

    data class Authenticated(val profile: Profile) : SessionState {
        val isAdmin: Boolean get() = profile.rol == UserRole.ADMIN
        val isStaff: Boolean get() = profile.rol == UserRole.ADMIN || profile.rol == UserRole.VENDEDOR

        /**
         * Estado degenerado documentado en el esquema: un vendedor sin
         * location_id no verá pedidos (RLS). La UI debe mostrar un aviso
         * y bloquear el POS hasta que un admin le asigne tienda.
         */
        val vendedorSinTienda: Boolean
            get() = profile.rol == UserRole.VENDEDOR && profile.locationId == null
    }

    /** Sesión válida pero profiles.activo = false: acceso revocado. */
    data object Inactive : SessionState

    /** Sesión válida pero el profile no pudo cargarse (red, RLS...). */
    data class Error(val error: DataError) : SessionState
}

interface AuthRepository {

    /** Fuente única de verdad de la sesión para toda la app. */
    val sessionState: StateFlow<SessionState>

    suspend fun signIn(email: String, password: String): DataResult<Unit>

    suspend fun signOut(): DataResult<Unit>

    /** Reintenta la carga del profile (para SessionState.Error). */
    suspend fun refreshProfile(): DataResult<Unit>
}
