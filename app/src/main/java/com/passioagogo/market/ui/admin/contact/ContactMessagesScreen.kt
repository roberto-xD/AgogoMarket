package com.passioagogo.market.ui.admin.contact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
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

data class ContactUiState(
    val pendingOnly: Boolean = false,
    val messages: List<ContactMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    fun onTogglePending(pendingOnly: Boolean) {
        _uiState.update { it.copy(pendingOnly = pendingOnly) }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = contactRepository.getMessages(_uiState.value.pendingOnly)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(isLoading = false, messages = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun ContactMessagesScreen(viewModel: ContactViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !state.pendingOnly,
                onClick = { viewModel.onTogglePending(false) },
                label = { Text("Todos") },
            )
            FilterChip(
                selected = state.pendingOnly,
                onClick = { viewModel.onTogglePending(true) },
                label = { Text("Sin atender") },
            )
            Spacer(Modifier.weight(1f))
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
                Text("Sin mensajes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> LazyColumn {
                items(state.messages, key = { it.id }) { message ->
                    MessageRow(message)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun MessageRow(message: ContactMessage) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
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
            listOfNotNull(message.email, message.createdAt?.take(10)).joinToString("  ·  "),
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
        AnimatedVisibility(visible = !expanded && message.mensaje.length > 100) {
            Text(
                "Toca para leer completo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
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
