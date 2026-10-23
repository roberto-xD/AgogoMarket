package com.passioagogo.market.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.push.PushTokenRepository
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val adminKeyStore: com.passioagogo.market.core.crypto.AdminKeyStore,
) : ViewModel() {

    val sessionState = authRepository.sessionState

    init {
        // Registra el token cada vez que hay sesión: cubre el login y
        // también el arranque con sesión persistida.
        viewModelScope.launch {
            authRepository.sessionState.collect { estado ->
                if (estado is SessionState.Authenticated) {
                    pushTokenRepository.sincronizar()
                }
            }
        }
    }

    fun onRetry() = viewModelScope.launch { authRepository.refreshProfile() }

    fun onSignOut() = viewModelScope.launch {
        // La clave privada no debe sobrevivir a la sesión: si otra persona
        // entra en este dispositivo, no abriría direcciones ajenas.
        adminKeyStore.bloquear()
        // Antes de cerrar: si no, el siguiente usuario de este teléfono
        // heredaría las notificaciones del anterior.
        pushTokenRepository.eliminarTokenActual()
        authRepository.signOut()
    }
}
