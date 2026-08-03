package com.passioagogo.market.ui.orders

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.passioagogo.market.domain.common.OrderStatus
import com.passioagogo.market.domain.sales.Order
import com.passioagogo.market.ui.pos.etiqueta
import java.text.NumberFormat
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

// ============ Historial ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    onOpenOrder: (String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (state.isAdmin) {
                var expanded by remember { mutableStateOf(false) }
                val selectedName = state.locations
                    .firstOrNull { it.id == state.locationId }?.nombre
                    ?: "Todas las ubicaciones"
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ubicación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Todas las ubicaciones") },
                            onClick = { expanded = false; viewModel.onLocationSelected(null) },
                        )
                        state.locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location.nombre) },
                                onClick = {
                                    expanded = false
                                    viewModel.onLocationSelected(location.id)
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.openOnly,
                    onClick = { viewModel.onToggleOpenOnly(true) },
                    label = { Text("Abiertos") },
                )
                FilterChip(
                    selected = !state.openOnly,
                    onClick = { viewModel.onToggleOpenOnly(false) },
                    label = { Text("Todos") },
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                }
            }
        }

        when {
            state.isLoading && state.orders.isEmpty() ->
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

            state.visibleOrders.isEmpty() -> Centered {
                Text("Sin pedidos", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            else -> LazyColumn {
                items(state.visibleOrders, key = { it.id }) { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenOrder(order.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "#${order.folio} · ${moneda.format(order.total)}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                listOfNotNull(
                                    "${order.items.sumOf { it.cantidad }} artículos",
                                    if (state.locationId == null)
                                        state.locationNames[order.locationId] else null,
                                ).joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AssistChip(
                            onClick = { onOpenOrder(order.id) },
                            label = { Text(order.estado.etiqueta) },
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

// ============ Detalle ============

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val order = state.order

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            state.isLoading -> Centered { CircularProgressIndicator() }
            order == null -> Centered {
                Text(state.errorMessage ?: "No se encontró el pedido")
            }
            else -> {
                Text(
                    "Pedido #${order.folio}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    listOfNotNull(
                        order.estado.etiqueta,
                        state.locationNames[order.locationId],
                    ).joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f)) {
                    items(order.items, key = { it.id }) { item ->
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
                                        "${item.cantidad} × ${moneda.format(item.precioUnitario)}",
                                    ).joinToString("  ·  "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                moneda.format(
                                    item.cantidad * item.precioUnitario - item.descuento
                                ),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        HorizontalDivider()
                    }
                }

                Spacer(Modifier.height(8.dp))
                TotalRow("Subtotal", order.subtotal)
                if (order.descuento > 0) TotalRow("Descuento", -order.descuento)
                if (order.costoEnvio > 0) TotalRow("Envío", order.costoEnvio)
                TotalRow("Total", order.total, bold = true)

                order.payments.forEach { payment ->
                    Text(
                        "Pago: ${moneda.format(payment.monto)} · ${payment.metodo.etiqueta}" +
                            (payment.referencia?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                        Text("Volver")
                    }
                    if (order.estado.esCancelable) {
                        Button(
                            onClick = viewModel::onAskCancel,
                            enabled = !state.isCancelling,
                            modifier = Modifier.weight(1f),
                        ) { Text("Cancelar pedido") }
                    }
                }
            }
        }
    }

    if (state.showCancelConfirm && order != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissCancel,
            title = { Text("Cancelar pedido #${order.folio}") },
            text = {
                Text(
                    if (order.estado == OrderStatus.PENDIENTE)
                        "El pedido se cancelará."
                    else
                        "El pedido se cancelará y el stock regresará a la ubicación."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmCancel) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissCancel) { Text("No") }
            },
        )
    }
}

@Composable
private fun TotalRow(label: String, amount: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            Modifier.weight(1f),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            moneda.format(amount),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
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
