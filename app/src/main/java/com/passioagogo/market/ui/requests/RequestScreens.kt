package com.passioagogo.market.ui.requests

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.requests.OrderRequest
import com.passioagogo.market.data.requests.OrderRequestDraft
import com.passioagogo.market.data.requests.OrderRequestRepository
import com.passioagogo.market.data.requests.RequestCart
import com.passioagogo.market.data.requests.RequestCartItem
import com.passioagogo.market.data.requests.RequestLine
import com.passioagogo.market.domain.common.RequestStatus
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

internal val RequestStatus.etiqueta: String
    get() = when (this) {
        RequestStatus.ENVIADA -> "Enviada"
        RequestStatus.ATENDIDA -> "Atendida"
        RequestStatus.RECHAZADA -> "Rechazada"
    }

// ============ Carrito / nueva solicitud ============

data class RequestCartUiState(
    val esCliente: Boolean = false,
    val items: List<RequestCartItem> = emptyList(),
    val clienteNombre: String = "",
    val clienteTelefono: String = "",
    val clienteEmail: String = "",
    val notas: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val folioEnviado: Long? = null,
) {
    val total: Double get() = items.sumOf { it.importe }

    val canSend: Boolean
        get() = !isSending && items.isNotEmpty() && clienteNombre.isNotBlank()
}

@HiltViewModel
class RequestCartViewModel @Inject constructor(
    private val cart: RequestCart,
    private val repository: OrderRequestRepository,
    authRepository: com.passioagogo.market.domain.auth.AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestCartUiState())
    val uiState: StateFlow<RequestCartUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value
            as? com.passioagogo.market.domain.auth.SessionState.Authenticated
        // El cliente pide para sí mismo: su nombre ya se conoce.
        _uiState.update {
            it.copy(
                esCliente = sesion?.isCliente == true,
                clienteNombre = if (sesion?.isCliente == true) {
                    sesion.profile.nombre
                } else "",
            )
        }
        viewModelScope.launch {
            cart.items.collect { mapa ->
                _uiState.update { it.copy(items = mapa.values.toList()) }
            }
        }
    }

    fun onNombre(v: String) = _uiState.update { it.copy(clienteNombre = v) }
    fun onTelefono(v: String) = _uiState.update { it.copy(clienteTelefono = v) }
    fun onEmail(v: String) = _uiState.update { it.copy(clienteEmail = v) }
    fun onNotas(v: String) = _uiState.update { it.copy(notas = v) }
    fun onQuantity(variantId: String, cantidad: Int) = cart.setQuantity(variantId, cantidad)
    fun onRemove(variantId: String) = cart.remove(variantId)
    fun onDismissEnviado() = _uiState.update { it.copy(folioEnviado = null) }

    fun onSend() {
        val state = _uiState.value
        if (!state.canSend) return
        _uiState.update { it.copy(isSending = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.createRequest(
                OrderRequestDraft(
                    clienteNombre = state.clienteNombre.trim(),
                    clienteTelefono = state.clienteTelefono.trim().ifBlank { null },
                    clienteEmail = state.clienteEmail.trim().ifBlank { null },
                    notas = state.notas.trim().ifBlank { null },
                    lines = state.items.map {
                        RequestLine(
                            variantId = it.variantId,
                            cantidad = it.cantidad,
                            precioEstimado = it.precio,
                        )
                    },
                )
            )
            when (result) {
                is DataResult.Success -> {
                    cart.clear()
                    _uiState.update {
                        RequestCartUiState(
                            esCliente = it.esCliente,
                            clienteNombre = if (it.esCliente) it.clienteNombre else "",
                            folioEnviado = result.data.folio,
                        )
                    }
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSending = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun RequestCartScreen(viewModel: RequestCartViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    "Carrito vacío.\nAbre un producto del catálogo y agrégalo.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(state.items, key = { it.variantId }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.producto, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOf(item.sku, moneda.format(item.precio))
                                    .joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { viewModel.onQuantity(item.variantId, item.cantidad - 1) },
                        ) { Icon(Icons.Filled.Remove, contentDescription = "Menos") }
                        Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)
                        IconButton(
                            onClick = { viewModel.onQuantity(item.variantId, item.cantidad + 1) },
                        ) { Icon(Icons.Filled.Add, contentDescription = "Más") }
                        IconButton(onClick = { viewModel.onRemove(item.variantId) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                        }
                    }
                    HorizontalDivider()
                }

                item(key = "datos") {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (state.esCliente) "Tus datos de contacto"
                        else "Datos del cliente",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.clienteNombre,
                        onValueChange = viewModel::onNombre,
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.clienteTelefono,
                        onValueChange = viewModel::onTelefono,
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.clienteEmail,
                        onValueChange = viewModel::onEmail,
                        label = { Text("Correo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.notas,
                        onValueChange = viewModel::onNotas,
                        label = {
                            Text(
                                if (state.esCliente) "Comentarios de tu pedido"
                                else "Notas para el administrador"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Total estimado", style = MaterialTheme.typography.labelMedium)
                    Text(
                        moneda.format(state.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Button(onClick = viewModel::onSend, enabled = state.canSend) {
                    Text(if (state.isSending) "Enviando…" else "Enviar solicitud")
                }
            }
            Text(
                if (state.esCliente)
                    "Te confirmaremos precios y disponibilidad antes de completar el pedido."
                else
                    "El administrador confirmará precios y disponibilidad al procesarla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    state.folioEnviado?.let { folio ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissEnviado,
            title = { Text("Solicitud enviada") },
            text = {
                Text(
                    if (state.esCliente)
                        "Tu pedido quedó registrado con el folio #$folio. " +
                            "Nos pondremos en contacto para completarlo."
                    else
                        "Quedó registrada con el folio #$folio. " +
                            "Un administrador la revisará para completar la venta."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissEnviado) { Text("Entendido") }
            },
        )
    }
}

// ============ Bandeja de solicitudes ============

data class RequestsUiState(
    val requests: List<OrderRequest> = emptyList(),
    val pendingOnly: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val repository: OrderRequestRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestsUiState())
    val uiState: StateFlow<RequestsUiState> = _uiState.asStateFlow()

    fun onTogglePending(pendingOnly: Boolean) {
        _uiState.update { it.copy(pendingOnly = pendingOnly) }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getRequests(_uiState.value.pendingOnly)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(isLoading = false, requests = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun RequestsListScreen(
    onOpenRequest: (String) -> Unit,
    viewModel: RequestsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.pendingOnly,
                onClick = { viewModel.onTogglePending(true) },
                label = { Text("Pendientes") },
            )
            FilterChip(
                selected = !state.pendingOnly,
                onClick = { viewModel.onTogglePending(false) },
                label = { Text("Todas") },
            )
        }

        when {
            state.isLoading && state.requests.isEmpty() ->
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

            state.errorMessage != null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                Alignment.Center,
            ) {
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

            state.requests.isEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                Alignment.Center,
            ) {
                Text("Sin solicitudes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> LazyColumn {
                items(state.requests, key = { it.id }) { request ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenRequest(request.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "#${request.folio} · ${request.clienteNombre}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "${request.items.sumOf { it.cantidad }} artículos · " +
                                    moneda.format(request.totalEstimado),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AssistChip(
                            onClick = { onOpenRequest(request.id) },
                            label = { Text(request.estado.etiqueta) },
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
