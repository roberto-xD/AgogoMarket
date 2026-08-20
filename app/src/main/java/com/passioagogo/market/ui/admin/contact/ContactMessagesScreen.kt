package com.passioagogo.market.ui.admin.contact

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.contact.ContactFilter
import com.passioagogo.market.data.contact.ContactMessage
import com.passioagogo.market.data.contact.ContactRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val ContactFilter.etiqueta: String
    get() = when (this) {
        ContactFilter.SIN_ATENDER -> "Sin atender"
        ContactFilter.ATENDIDOS -> "Atendidos"
        ContactFilter.TODOS -> "Todos"
    }

data class ContactUiState(
    val filtro: ContactFilter = ContactFilter.SIN_ATENDER,
    val messages: List<ContactMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Mensaje pendiente de confirmar archivado. */
    val porArchivar: ContactMessage? = null,
)

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    fun onFiltro(filtro: ContactFilter) {
        _uiState.update { it.copy(filtro = filtro) }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = contactRepository.getMessages(_uiState.value.filtro)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(isLoading = false, messages = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onMarcarAtendido(message: ContactMessage) {
        viewModelScope.launch {
            when (val r = contactRepository.marcarAtendido(message.id, !message.atendido)) {
                is DataResult.Success -> refresh()
                is DataResult.Error ->
                    _uiState.update { it.copy(errorMessage = r.error.toMessage()) }
            }
        }
    }

    fun onAskArchivar(message: ContactMessage) =
        _uiState.update { it.copy(porArchivar = message) }

    fun onDismissArchivar() = _uiState.update { it.copy(porArchivar = null) }

    fun onArchivar() {
        val message = _uiState.value.porArchivar ?: return
        _uiState.update { it.copy(porArchivar = null) }
        viewModelScope.launch {
            when (val r = contactRepository.archivar(message.id)) {
                is DataResult.Success -> refresh()
                is DataResult.Error ->
                    _uiState.update { it.copy(errorMessage = r.error.toMessage()) }
            }
        }
    }
}

/** Abre la app de correo con el destinatario y asunto ya puestos. */
private fun enviarCorreo(context: Context, message: ContactMessage) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        // ACTION_SENDTO con esquema mailto: solo lo resuelven clientes de
        // correo, así que el selector no ofrece apps irrelevantes.
        data = Uri.parse("mailto:${message.email}")
        putExtra(Intent.EXTRA_SUBJECT, "Passion A Gogo · Respuesta a tu mensaje")
        putExtra(
            Intent.EXTRA_TEXT,
            "Hola ${message.nombre}:\n\n\n" +
                "-------------------------\n" +
                "Tu mensaje:\n${message.mensaje}",
        )
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Enviar correo"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No hay una app de correo configurada",
            Toast.LENGTH_SHORT,
        ).show()
    }
}

@Composable
fun ContactMessagesScreen(viewModel: ContactViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ContactFilter.entries.forEach { filtro ->
                FilterChip(
                    selected = state.filtro == filtro,
                    onClick = { viewModel.onFiltro(filtro) },
                    label = { Text(filtro.etiqueta) },
                )
            }
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        }

        when {
            state.isLoading && state.messages.isEmpty() ->
                Centered { CircularProgressIndicator() }

            state.errorMessage != null -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::refresh) { Text("Reintentar") }
                }
            }

            state.messages.isEmpty() -> Centered {
                Text(
                    when (state.filtro) {
                        ContactFilter.SIN_ATENDER -> "No hay mensajes pendientes"
                        ContactFilter.ATENDIDOS -> "Aún no hay mensajes atendidos"
                        ContactFilter.TODOS -> "Sin mensajes"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyColumn {
                items(state.messages, key = { it.id }) { message ->
                    MessageRow(
                        message = message,
                        onMarcarAtendido = { viewModel.onMarcarAtendido(message) },
                        onArchivar = { viewModel.onAskArchivar(message) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    state.porArchivar?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissArchivar,
            title = { Text("Eliminar de la bandeja") },
            text = {
                Text(
                    "El mensaje de ${message.nombre} dejará de aparecer aquí, " +
                        "pero se conserva en la base de datos."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onArchivar) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissArchivar) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun MessageRow(
    message: ContactMessage,
    onMarcarAtendido: () -> Unit,
    onArchivar: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    message.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (!message.atendido) {
                    Text(
                        "Sin atender",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Text(
                listOfNotNull(message.email, message.createdAt?.take(10))
                    .joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!expanded && message.mensaje.length > 100) {
                Text(
                    "Toca para leer completo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Box {
            IconButton(onClick = { menuAbierto = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Acciones")
            }
            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (message.atendido) "Marcar como pendiente"
                            else "Marcar como atendido"
                        )
                    },
                    onClick = { menuAbierto = false; onMarcarAtendido() },
                )
                DropdownMenuItem(
                    text = { Text("Enviar correo") },
                    onClick = {
                        menuAbierto = false
                        enviarCorreo(context, message)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = { menuAbierto = false; onArchivar() },
                )
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
