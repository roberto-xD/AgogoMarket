package com.passioagogo.market.ui.requests

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.preferences.UserPreferences
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.requests.OrderRequest
import com.passioagogo.market.data.requests.OrderRequestRepository
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.common.PaymentMethod
import com.passioagogo.market.domain.common.RequestStatus
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.sales.CartLine
import com.passioagogo.market.domain.sales.CheckoutRequest
import com.passioagogo.market.domain.sales.SalesRepository
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.transfers.VariantInfo
import com.passioagogo.market.ui.inventory.transfers.variantIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val monedaDetalle: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

private fun PaymentMethod.rotulo(): String = when (this) {
    PaymentMethod.EFECTIVO -> "Efectivo"
    PaymentMethod.TRANSFERENCIA -> "Transferencia"
    PaymentMethod.TARJETA -> "Tarjeta"
}

data class RequestDetailUiState(
    val request: OrderRequest? = null,
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val esAdmin: Boolean = false,
    val locations: List<Location> = emptyList(),
    val locationId: String? = null,
    val metodo: PaymentMethod = PaymentMethod.EFECTIVO,
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val showConvertDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val errorMessage: String? = null,
    val folioPedido: Long? = null,
) {
    val puedeAtender: Boolean
        get() = esAdmin && request?.estado == RequestStatus.ENVIADA
}

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val requestRepository: OrderRequestRepository,
    private val salesRepository: SalesRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    private val userPreferences: UserPreferences,
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value as? SessionState.Authenticated
        val esAdmin = sesion?.isAdmin == true
        _uiState.update { it.copy(esAdmin = esAdmin) }

        viewModelScope.launch {
            _uiState.update { it.copy(variantIndex = catalogRepository.variantIndex()) }
            if (esAdmin) {
                val locs = locationRepository.getLocations()
                val guardada = userPreferences.activeLocationId.first()
                val lista = (locs as? DataResult.Success)?.data ?: emptyList()
                _uiState.update {
                    it.copy(
                        locations = lista,
                        locationId = guardada?.takeIf { id -> lista.any { l -> l.id == id } },
                    )
                }
            }
            load()
        }
    }

    fun onLocationSelected(id: String) {
        _uiState.update { it.copy(locationId = id) }
        viewModelScope.launch { userPreferences.setActiveLocation(id) }
    }

    fun onMetodo(m: PaymentMethod) = _uiState.update { it.copy(metodo = m) }
    fun onAskConvert() = _uiState.update { it.copy(showConvertDialog = true) }
    fun onDismissConvert() = _uiState.update { it.copy(showConvertDialog = false) }
    fun onAskReject() = _uiState.update { it.copy(showRejectDialog = true) }
    fun onDismissReject() = _uiState.update { it.copy(showRejectDialog = false) }
    fun onDismissFolio() = _uiState.update { it.copy(folioPedido = null) }

    /**
     * Convierte la solicitud en venta real: el checkout recotiza cada línea
     * con `fn_agregar_item`, descuenta stock y cobra. Solo si eso sale bien
     * se marca la solicitud como atendida.
     */
    fun onConvert() {
        val state = _uiState.value
        val request = state.request ?: return
        val locationId = state.locationId ?: return
        _uiState.update {
            it.copy(isProcessing = true, showConvertDialog = false, errorMessage = null)
        }
        viewModelScope.launch {
            val venta = salesRepository.checkout(
                CheckoutRequest(
                    locationId = locationId,
                    lines = request.items.map {
                        CartLine(variantId = it.variantId, cantidad = it.cantidad)
                    },
                    metodo = state.metodo,
                    notas = "Solicitud #${request.folio} · ${request.clienteNombre}",
                )
            )
            when (venta) {
                is DataResult.Error -> _uiState.update {
                    it.copy(isProcessing = false, errorMessage = venta.error.toMessage())
                }
                is DataResult.Success -> {
                    val pedido = venta.data
                    // La venta ya existe: si el enlace fallara, el pedido no
                    // se pierde, solo quedaría la solicitud sin marcar.
                    requestRepository.markAttended(request.id, pedido.id)
                    _uiState.update { it.copy(isProcessing = false, folioPedido = pedido.folio) }
                    load()
                }
            }
        }
    }

    fun onReject(motivo: String) {
        val request = _uiState.value.request ?: return
        _uiState.update {
            it.copy(isProcessing = true, showRejectDialog = false, errorMessage = null)
        }
        viewModelScope.launch {
            when (val r = requestRepository.reject(request.id, motivo)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isProcessing = false) }
                    load()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isProcessing = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true) }
        val result = requestRepository.getRequest(requestId)
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, request = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    onBack: () -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val request = state.request

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (request == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(state.errorMessage ?: "Solicitud no encontrada")
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Solicitud #${request.folio}", style = MaterialTheme.typography.titleLarge)
        Text(
            request.estado.etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))
        Text("Cliente", style = MaterialTheme.typography.labelLarge)
        Text(request.clienteNombre, style = MaterialTheme.typography.bodyLarge)
        listOfNotNull(request.clienteTelefono, request.clienteEmail)
            .takeIf { it.isNotEmpty() }
            ?.let {
                Text(
                    it.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        request.notas?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(request.items, key = { it.id }) { item ->
                val info = state.variantIndex[item.variantId]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            info?.producto ?: item.variantId,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            listOfNotNull(
                                info?.sku,
                                "${item.cantidad} × ${monedaDetalle.format(item.precioEstimado)}",
                            ).joinToString("  ·  "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        monedaDetalle.format(item.cantidad * item.precioEstimado),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                HorizontalDivider()
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Total estimado", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(monedaDetalle.format(request.totalEstimado), fontWeight = FontWeight.Bold)
        }

        request.motivoRechazo?.let {
            Text(
                "Rechazada: $it",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (state.puedeAtender) {
            Text(
                "Al convertirla se recotizan los precios con las promociones " +
                    "vigentes y se descuenta el stock.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = viewModel::onAskReject,
                    enabled = !state.isProcessing,
                    modifier = Modifier.weight(1f),
                ) { Text("Rechazar") }
                Button(
                    onClick = viewModel::onAskConvert,
                    enabled = !state.isProcessing,
                    modifier = Modifier.weight(1f),
                ) { Text(if (state.isProcessing) "Procesando…" else "Convertir en venta") }
            }
        } else {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }

    if (state.showConvertDialog) {
        var expandedLoc by remember { mutableStateOf(false) }
        var expandedPago by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = viewModel::onDismissConvert,
            title = { Text("Convertir en venta") },
            text = {
                Column {
                    ExposedDropdownMenuBox(expandedLoc, { expandedLoc = it }) {
                        OutlinedTextField(
                            value = state.locations
                                .firstOrNull { it.id == state.locationId }?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Despachar desde") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandedLoc)
                            },
                            modifier = Modifier.menuAnchor(),
                        )
                        ExposedDropdownMenu(expandedLoc, { expandedLoc = false }) {
                            state.locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.nombre) },
                                    onClick = {
                                        viewModel.onLocationSelected(loc.id)
                                        expandedLoc = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expandedPago, { expandedPago = it }) {
                        OutlinedTextField(
                            value = state.metodo.rotulo(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Método de pago") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandedPago)
                            },
                            modifier = Modifier.menuAnchor(),
                        )
                        ExposedDropdownMenu(expandedPago, { expandedPago = false }) {
                            PaymentMethod.entries.forEach { pm ->
                                DropdownMenuItem(
                                    text = { Text(pm.rotulo()) },
                                    onClick = { viewModel.onMetodo(pm); expandedPago = false },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = state.locationId != null,
                    onClick = viewModel::onConvert,
                ) { Text("Confirmar venta") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissConvert) { Text("Cancelar") }
            },
        )
    }

    if (state.showRejectDialog) {
        var motivo by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::onDismissReject,
            title = { Text("Rechazar solicitud") },
            text = {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo") },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = motivo.isNotBlank(),
                    onClick = { viewModel.onReject(motivo.trim()) },
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissReject) { Text("Cancelar") }
            },
        )
    }

    state.folioPedido?.let { folio ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissFolio,
            title = { Text("Venta registrada") },
            text = { Text("Se creó el pedido #$folio y la solicitud quedó atendida.") },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissFolio) { Text("Entendido") }
            },
        )
    }
}
