package com.passioagogo.market.ui.inventory.requests

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.data.transferrequests.TransferRequest
import com.passioagogo.market.data.transferrequests.TransferRequestStatus
import com.passioagogo.market.data.transferrequests.TransferRequestType

// ============ Lista ============

@Composable
fun TransferRequestsScreen(
    onOpenRequest: (String) -> Unit,
    onNewRequest: () -> Unit,
    viewModel: TransferRequestsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.openOnly,
                    onClick = { viewModel.onToggleOpen(true) },
                    label = { Text("Pendientes") },
                )
                FilterChip(
                    selected = !state.openOnly,
                    onClick = { viewModel.onToggleOpen(false) },
                    label = { Text("Todas") },
                )
            }

            when {
                state.isLoading && state.requests.isEmpty() ->
                    Centrado { CircularProgressIndicator() }

                state.errorMessage != null -> Centrado {
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

                state.requests.isEmpty() -> Centrado {
                    Text(
                        "Sin solicitudes de transferencia",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> LazyColumn {
                    items(state.requests, key = { it.id }) { request ->
                        FilaSolicitud(
                            request = request,
                            nombres = state.locationNames,
                            meToca = state.meTocaResolver(request),
                            onClick = { onOpenRequest(request.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewRequest,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva solicitud")
        }
    }
}

@Composable
private fun FilaSolicitud(
    request: TransferRequest,
    nombres: Map<String, String>,
    meToca: Boolean,
    onClick: () -> Unit,
) {
    val origen = request.fromLocationId?.let { nombres[it] } ?: "Por asignar"
    val destino = nombres[request.toLocationId] ?: "…"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "#${request.folio} · $origen → $destino",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                listOfNotNull(
                    request.tipo.etiqueta,
                    "${request.items.sumOf { it.cantidadSolicitada }} artículos",
                    request.solicitanteNombre?.let { "pidió $it" },
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (meToca) {
                Text(
                    "Requiere tu respuesta",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        AssistChip(onClick = onClick, label = { Text(request.estado.etiqueta) })
    }
}

// ============ Creación ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransferRequestScreen(
    onCreated: () -> Unit,
    viewModel: CreateTransferRequestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Centrado { CircularProgressIndicator() }
        return
    }
    if (state.miTienda == null) {
        Centrado {
            Text(
                "Necesitas una tienda asignada para solicitar transferencias.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.tipo == TransferRequestType.PEDIDO,
                onClick = { viewModel.onTipo(TransferRequestType.PEDIDO) },
                label = { Text("Pedir") },
            )
            FilterChip(
                selected = state.tipo == TransferRequestType.DEVOLUCION,
                onClick = { viewModel.onTipo(TransferRequestType.DEVOLUCION) },
                label = { Text("Devolver") },
            )
        }
        Text(
            when (state.tipo) {
                TransferRequestType.PEDIDO ->
                    "Pides mercancía para tu tienda. La autoriza quien la envía."
                TransferRequestType.DEVOLUCION ->
                    "Devuelves mercancía de tu tienda. La autoriza quien la recibe."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        val otras = state.locations.filter { it.id != state.miTienda }
        when (state.tipo) {
            TransferRequestType.PEDIDO -> {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = otras.firstOrNull { it.id == state.fromLocationId }?.nombre
                            ?: "Que lo decida administración",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pedir a") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        // Origen abierto: evita triangular por bodega cuando
                        // no se sabe qué tienda tiene existencia.
                        DropdownMenuItem(
                            text = { Text("Que lo decida administración") },
                            onClick = { viewModel.onFrom(null); expanded = false },
                        )
                        otras.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.nombre) },
                                onClick = { viewModel.onFrom(loc.id); expanded = false },
                            )
                        }
                    }
                }
            }
            TransferRequestType.DEVOLUCION -> {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = otras.firstOrNull { it.id == state.toLocationId }?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Devolver a") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        otras.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.nombre) },
                                onClick = { viewModel.onTo(loc.id); expanded = false },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQuery,
            label = { Text("Buscar producto o SKU") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(Modifier.weight(1f)) {
            items(state.searchResults, key = { "s-${it.variantId}" }) { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAddVariant(info.variantId) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(info.producto)
                        Text(
                            listOfNotNull(
                                info.sku,
                                if (state.tipo == TransferRequestType.DEVOLUCION) {
                                    "tienes ${state.miStock[info.variantId] ?: 0}"
                                } else null,
                            ).joinToString("  ·  "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.Add, contentDescription = "Agregar")
                }
                HorizontalDivider()
            }

            if (state.query.isBlank()) {
                items(state.lines.entries.toList(), key = { "l-${it.key}" }) { (id, cantidad) ->
                    val info = state.variantIndex[id]
                    val excede = state.excedeExistencia(id, cantidad)
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(info?.producto ?: id)
                                Text(
                                    info?.sku ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            OutlinedTextField(
                                value = cantidad.toString(),
                                onValueChange = {
                                    viewModel.onCantidad(id, it.toIntOrNull() ?: 0)
                                },
                                label = { Text("Cant.") },
                                singleLine = true,
                                isError = excede,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                modifier = Modifier.width(90.dp),
                            )
                            IconButton(onClick = { viewModel.onCantidad(id, 0) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                            }
                        }
                        if (excede) {
                            Text(
                                "Solo tienes ${state.miStock[id] ?: 0} en existencia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        // Al pedir, saber quién tiene evita pedir a quien no
                        if (state.tipo == TransferRequestType.PEDIDO) {
                            val donde = state.disponibilidad[id].orEmpty()
                            Text(
                                if (donde.isEmpty()) "Sin existencia en ninguna ubicación"
                                else "Disponible en " + donde.joinToString(", ") {
                                    "${it.ubicacion} (${it.cantidad})"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (donde.isEmpty()) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HorizontalDivider(Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.notas,
            onValueChange = viewModel::onNotas,
            label = { Text("Notas") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSaving) "Enviando…" else "Enviar solicitud")
        }
    }

    state.creada?.let { folio ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onDismissCreada()
                onCreated()
            },
            title = { Text("Solicitud enviada") },
            text = { Text("Quedó registrada con el folio #$folio, a la espera de respuesta.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDismissCreada()
                    onCreated()
                }) { Text("Entendido") }
            },
        )
    }
}

// ============ Detalle ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferRequestDetailScreen(
    onBack: () -> Unit,
    viewModel: TransferRequestDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val request = state.request

    if (state.isLoading) {
        Centrado { CircularProgressIndicator() }
        return
    }
    if (request == null) {
        Centrado { Text(state.errorMessage ?: "Solicitud no encontrada") }
        return
    }

    val origen = state.origenEfectivo?.let { state.locationNames[it] } ?: "Por asignar"
    val destino = state.locationNames[request.toLocationId] ?: "…"

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Solicitud #${request.folio}", style = MaterialTheme.typography.titleLarge)
        Text(
            "${request.tipo.etiqueta} · ${request.estado.etiqueta}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("$origen  →  $destino", style = MaterialTheme.typography.bodyLarge)
        request.solicitanteNombre?.let {
            Text(
                "Pedida por $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        request.notas?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }

        // Un pedido abierto necesita que quien resuelve elija de dónde sale
        if (state.puedeResolver && state.requiereOrigen) {
            Spacer(Modifier.height(12.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                OutlinedTextField(
                    value = state.locations
                        .firstOrNull { it.id == state.origenElegido }?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Enviar desde") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    state.locations
                        .filter { it.id != request.toLocationId }
                        .forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.nombre) },
                                onClick = {
                                    viewModel.onOrigenElegido(loc.id)
                                    expanded = false
                                },
                            )
                        }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(request.items, key = { it.id }) { item ->
                val info = state.variantIndex[item.variantId]
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                info?.producto ?: item.variantId,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                listOfNotNull(
                                    info?.sku,
                                    "pidió ${item.cantidadSolicitada}",
                                    state.stockOrigen[item.variantId]
                                        ?.let { "hay $it" },
                                ).joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (state.puedeResolver) {
                            OutlinedTextField(
                                value = state.aprobadas[item.variantId].orEmpty(),
                                onValueChange = {
                                    viewModel.onCantidadAprobada(item.variantId, it)
                                },
                                label = { Text("Envío") },
                                singleLine = true,
                                isError = state.excede(item.variantId),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                modifier = Modifier.width(96.dp),
                            )
                        } else {
                            Text(
                                item.cantidadAprobada?.let { "×$it" }
                                    ?: "×${item.cantidadSolicitada}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    if (state.excede(item.variantId)) {
                        Text(
                            "Supera la existencia del origen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                HorizontalDivider()
            }
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

        if (state.puedeResolver) {
            Text(
                "Al aceptar se crea la transferencia; el stock se moverá al enviarla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = viewModel::onAskRechazo,
                    enabled = !state.isProcessing,
                    modifier = Modifier.weight(1f),
                ) { Text("Rechazar") }
                Button(
                    onClick = viewModel::onAceptar,
                    enabled = state.puedeAceptar,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.isProcessing) "Procesando…" else "Aceptar")
                }
            }
        } else if (state.puedeCancelar) {
            OutlinedButton(
                onClick = viewModel::onCancelar,
                enabled = !state.isProcessing,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Cancelar solicitud") }
        } else {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }

    if (state.showRechazo) {
        var motivo by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::onDismissRechazo,
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
                    onClick = { viewModel.onRechazar(motivo.trim()) },
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissRechazo) { Text("Cancelar") }
            },
        )
    }

    if (state.aceptada) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissAceptada,
            title = { Text("Solicitud aceptada") },
            text = {
                Text(
                    "Se creó la transferencia correspondiente. Encuéntrala en " +
                        "Transferencias para enviarla cuando prepares la mercancía."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissAceptada) { Text("Entendido") }
            },
        )
    }
}

@Composable
private fun Centrado(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
