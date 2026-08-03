package com.passioagogo.market.ui.inventory.transfers

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.passioagogo.market.domain.common.TransferStatus
import com.passioagogo.market.domain.inventory.StockTransfer

internal val TransferStatus.etiqueta: String
    get() = when (this) {
        TransferStatus.PENDIENTE -> "Pendiente"
        TransferStatus.EN_TRANSITO -> "En tránsito"
        TransferStatus.RECIBIDA -> "Recibida"
        TransferStatus.CANCELADA -> "Cancelada"
    }

// ============ Lista ============

@Composable
fun TransfersListScreen(
    onOpenTransfer: (String) -> Unit,
    onCreateTransfer: () -> Unit,
    viewModel: TransfersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Refresca al volver del detalle o de creación
    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.openOnly,
                    onClick = { viewModel.onToggleOpenOnly(true) },
                    label = { Text("Abiertas") },
                )
                FilterChip(
                    selected = !state.openOnly,
                    onClick = { viewModel.onToggleOpenOnly(false) },
                    label = { Text("Todas") },
                )
            }

            when {
                state.isLoading && state.transfers.isEmpty() ->
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

                state.transfers.isEmpty() -> Centered {
                    Text(
                        "Sin transferencias",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> LazyColumn {
                    items(state.transfers, key = { it.id }) { transfer ->
                        TransferRow(
                            transfer = transfer,
                            locationNames = state.locationNames,
                            onClick = { onOpenTransfer(transfer.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateTransfer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva transferencia")
        }
    }
}

@Composable
private fun TransferRow(
    transfer: StockTransfer,
    locationNames: Map<String, String>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    locationNames[transfer.fromLocationId] ?: "…",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 6.dp).width(16.dp),
                )
                Text(
                    locationNames[transfer.toLocationId] ?: "…",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text(
                text = "${transfer.items.sumOf { it.cantidad }} artículos" +
                    (transfer.notas?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AssistChip(onClick = onClick, label = { Text(transfer.estado.etiqueta) })
    }
}

// ============ Detalle ============

@Composable
fun TransferDetailScreen(
    onBack: () -> Unit,
    viewModel: TransferDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val transfer = state.transfer

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            state.isLoading -> Centered { CircularProgressIndicator() }
            transfer == null -> Centered {
                Text(state.errorMessage ?: "No se encontró la transferencia")
            }
            else -> {
                Text(
                    "${state.locationNames[transfer.fromLocationId] ?: "…"}  →  " +
                        (state.locationNames[transfer.toLocationId] ?: "…"),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    transfer.estado.etiqueta +
                        (transfer.notas?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f)) {
                    items(transfer.items, key = { it.id }) { item ->
                        val info = state.variantIndex[item.variantId]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    info?.producto ?: item.variantId,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    listOfNotNull(info?.sku, info?.atributos?.ifBlank { null })
                                        .joinToString("  ·  "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "×${item.cantidad}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        HorizontalDivider()
                    }
                }

                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (transfer.estado) {
                        TransferStatus.PENDIENTE -> {
                            OutlinedButton(
                                onClick = viewModel::cancel,
                                enabled = !state.isActing,
                                modifier = Modifier.weight(1f),
                            ) { Text("Cancelar") }
                            Button(
                                onClick = viewModel::send,
                                enabled = !state.isActing,
                                modifier = Modifier.weight(1f),
                            ) { Text("Enviar") }
                        }
                        TransferStatus.EN_TRANSITO -> {
                            OutlinedButton(
                                onClick = viewModel::cancel,
                                enabled = !state.isActing,
                                modifier = Modifier.weight(1f),
                            ) { Text("Cancelar") }
                            Button(
                                onClick = viewModel::receive,
                                enabled = !state.isActing,
                                modifier = Modifier.weight(1f),
                            ) { Text("Recibir") }
                        }
                        else -> OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                        ) { Text("Volver") }
                    }
                }
            }
        }
    }
}

// ============ Creación ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransferScreen(
    onCreated: () -> Unit,
    viewModel: CreateTransferViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.createdId) {
        if (state.createdId != null) onCreated()
    }

    if (state.isLoading) {
        Centered { CircularProgressIndicator() }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LocationDropdown(
            label = "Origen",
            locations = state.locations,
            selectedId = state.fromLocationId,
            onSelected = viewModel::onFromSelected,
        )
        Spacer(Modifier.height(8.dp))
        LocationDropdown(
            label = "Destino",
            locations = state.locations,
            selectedId = state.toLocationId,
            onSelected = viewModel::onToSelected,
        )
        if (state.fromLocationId != null && state.fromLocationId == state.toLocationId) {
            Text(
                "El origen y el destino deben ser distintos",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Agregar producto (nombre o SKU)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(Modifier.weight(1f)) {
            // Resultados de búsqueda
            items(state.searchResults, key = { "s-${it.variantId}" }) { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAddVariant(info.variantId) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(info.producto, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            listOf(info.sku, info.atributos).filter { it.isNotBlank() }
                                .joinToString("  ·  "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.Add, contentDescription = "Agregar")
                }
                HorizontalDivider()
            }

            // Líneas ya agregadas
            if (state.query.isBlank()) {
                items(state.lines.entries.toList(), key = { "l-${it.key}" }) { (variantId, cantidad) ->
                    val info = state.variantIndex[variantId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(info?.producto ?: variantId)
                            Text(
                                info?.sku ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = cantidad.toString(),
                            onValueChange = { text ->
                                viewModel.onQuantityChange(
                                    variantId,
                                    text.toIntOrNull() ?: 0,
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.width(80.dp),
                        )
                        IconButton(onClick = { viewModel.onQuantityChange(variantId, 0) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                        }
                    }
                }
            }
        }

        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        Button(
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSaving) "Guardando…" else "Crear transferencia")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDropdown(
    label: String,
    locations: List<com.passioagogo.market.domain.inventory.Location>,
    selectedId: String?,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = locations.firstOrNull { it.id == selectedId }?.nombre ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.nombre) },
                    onClick = {
                        expanded = false
                        onSelected(location.id)
                    },
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
