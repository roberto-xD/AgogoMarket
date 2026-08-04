package com.passioagogo.market.ui.admin.purchases

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.common.PurchaseStatus
import java.text.NumberFormat
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

// ============ Lista ============

@Composable
fun PurchasesListScreen(
    onOpenPurchase: (String) -> Unit,
    onCreatePurchase: () -> Unit,
    viewModel: PurchasesViewModel = hiltViewModel(),
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
                    selected = state.pendingOnly,
                    onClick = { viewModel.onTogglePendingOnly(true) },
                    label = { Text("Pendientes") },
                )
                FilterChip(
                    selected = !state.pendingOnly,
                    onClick = { viewModel.onTogglePendingOnly(false) },
                    label = { Text("Todas") },
                )
            }

            when {
                state.isLoading && state.purchases.isEmpty() ->
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

                state.purchases.isEmpty() -> Centered {
                    Text("Sin compras", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                else -> LazyColumn {
                    items(state.purchases, key = { it.id }) { purchase ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPurchase(purchase.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "#${purchase.folio} · " +
                                        (state.supplierNames[purchase.supplierId] ?: "…"),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    listOfNotNull(
                                        state.locationNames[purchase.locationId],
                                        moneda.format(purchase.total),
                                    ).joinToString("  ·  "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            AssistChip(
                                onClick = { onOpenPurchase(purchase.id) },
                                label = { Text(purchase.estado.etiqueta) },
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreatePurchase,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva compra")
        }
    }
}

// ============ Detalle ============

@Composable
fun PurchaseDetailScreen(
    onBack: () -> Unit,
    viewModel: PurchaseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val purchase = state.purchase

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            state.isLoading -> Centered { CircularProgressIndicator() }
            purchase == null -> Centered {
                Text(state.errorMessage ?: "No se encontró la compra")
            }
            else -> {
                Text(
                    "Compra #${purchase.folio}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    listOfNotNull(
                        purchase.estado.etiqueta,
                        state.supplierName,
                        state.locationName,
                    ).joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                purchase.notas?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f)) {
                    items(purchase.items, key = { it.id }) { item ->
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
                                        "${item.cantidad} × ${moneda.format(item.costoUnitario)}",
                                    ).joinToString("  ·  "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                moneda.format(item.cantidad * item.costoUnitario),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        HorizontalDivider()
                    }
                }

                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(moneda.format(purchase.total), fontWeight = FontWeight.Bold)
                }

                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                        Text("Volver")
                    }
                    if (purchase.estado == PurchaseStatus.PENDIENTE) {
                        OutlinedButton(
                            onClick = { viewModel.onAsk(PurchaseAction.CANCELAR) },
                            enabled = !state.isActing,
                            modifier = Modifier.weight(1f),
                        ) { Text("Cancelar") }
                        Button(
                            onClick = { viewModel.onAsk(PurchaseAction.RECIBIR) },
                            enabled = !state.isActing,
                            modifier = Modifier.weight(1f),
                        ) { Text("Recibir") }
                    }
                }
            }
        }
    }

    state.confirmAction?.let { action ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissConfirm,
            title = {
                Text(
                    when (action) {
                        PurchaseAction.RECIBIR -> "Recibir compra"
                        PurchaseAction.CANCELAR -> "Cancelar compra"
                    }
                )
            },
            text = {
                Text(
                    when (action) {
                        PurchaseAction.RECIBIR ->
                            "Se sumará el stock a la ubicación destino y se " +
                                "actualizará el costo de las variantes."
                        PurchaseAction.CANCELAR -> "La compra se cancelará sin mover stock."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirm) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissConfirm) { Text("Volver") }
            },
        )
    }
}

// ============ Creación ============

@Composable
fun CreatePurchaseScreen(
    onCreated: () -> Unit,
    viewModel: CreatePurchaseViewModel = hiltViewModel(),
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
        SimpleDropdown(
            label = "Proveedor",
            options = state.suppliers.map { it.id to it.nombre },
            selectedId = state.supplierId,
            onSelected = viewModel::onSupplierSelected,
        )
        Spacer(Modifier.height(8.dp))
        SimpleDropdown(
            label = "Ubicación destino",
            options = state.locations.map { it.id to it.nombre },
            selectedId = state.locationId,
            onSelected = viewModel::onLocationSelected,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Agregar producto (nombre o SKU)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(Modifier.weight(1f)) {
            items(state.searchResults, key = { "s-${it.variantId}" }) { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAddVariant(info) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(info.producto, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            listOf(info.sku, "Costo actual ${moneda.format(info.costo)}")
                                .joinToString("  ·  "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.Add, contentDescription = "Agregar")
                }
                HorizontalDivider()
            }

            if (state.query.isBlank()) {
                items(
                    state.lines.entries.toList(),
                    key = { "l-${it.key}" },
                ) { (variantId, line) ->
                    val info = state.variantIndex[variantId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            value = line.cantidad.toString(),
                            onValueChange = { text ->
                                viewModel.onLineChange(
                                    variantId,
                                    text.toIntOrNull() ?: 0,
                                    line.costo,
                                )
                            },
                            label = { Text("Cant.") },
                            singleLine = true,
                            modifier = Modifier.width(72.dp),
                        )
                        OutlinedTextField(
                            value = line.costo,
                            onValueChange = { text ->
                                viewModel.onLineChange(variantId, line.cantidad, text)
                            },
                            label = { Text("Costo") },
                            singleLine = true,
                            isError = line.costo.toDoubleOrNull() == null,
                            modifier = Modifier.width(96.dp),
                        )
                        IconButton(onClick = { viewModel.onRemoveLine(variantId) }) {
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
            Text(if (state.isSaving) "Guardando…" else "Crear compra")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selectedId }?.second ?: ""

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
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelected(id)
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
