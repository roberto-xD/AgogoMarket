package com.passioagogo.market.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.ui.auth.LoginScreen
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.navigation.AppScaffold
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val sessionState = authRepository.sessionState

    fun onRetry() = viewModelScope.launch { authRepository.refreshProfile() }
    fun onSignOut() = viewModelScope.launch { authRepository.signOut() }
}

/**
 * Raíz de la app: enruta según SessionState. La UI nunca consulta
 * Auth directamente — solo observa este estado.
 */
@Composable
fun AppRoot(viewModel: SessionViewModel = hiltViewModel()) {
    val session by viewModel.sessionState.collectAsState()

    // Última sesión válida conocida: evita desmontar el AppScaffold (y con
    // él el back stack de navegación) mientras la sesión pasa por estados
    // transitorios, p. ej. al regresar la app desde segundo plano.
    var lastAuthenticated by remember { mutableStateOf<SessionState.Authenticated?>(null) }
    LaunchedEffect(session) {
        when (session) {
            is SessionState.Authenticated -> lastAuthenticated = session as SessionState.Authenticated
            SessionState.NotAuthenticated, SessionState.Inactive -> lastAuthenticated = null
            else -> Unit
        }
    }

    when (val state = session) {
        SessionState.Initializing -> {
            val cached = lastAuthenticated
            if (cached != null) {
                AppScaffold(session = cached, onSignOut = viewModel::onSignOut)
            } else {
                LoadingScreen()
            }
        }
        SessionState.NotAuthenticated -> LoginScreen()
        is SessionState.Authenticated -> AppScaffold(
            session = state,
            onSignOut = viewModel::onSignOut,
        )
        SessionState.Inactive -> MessageScreen(
            title = "Cuenta desactivada",
            message = "Tu acceso fue revocado. Contacta a un administrador.",
            actionLabel = "Cerrar sesión",
            onAction = viewModel::onSignOut,
        )
        is SessionState.Error -> MessageScreen(
            title = "No pudimos cargar tu perfil",
            message = state.error.toMessage(),
            actionLabel = "Reintentar",
            onAction = viewModel::onRetry,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageScreen(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}
