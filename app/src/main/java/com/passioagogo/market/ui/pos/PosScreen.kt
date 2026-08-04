package com.passioagogo.market.ui.pos

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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.common.PaymentMethod
import com.passioagogo.market.domain.sales.Order
import java.text.NumberFormat
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

internal val PaymentMethod.etiqueta: String
    get() = when (this) {
        PaymentMethod.EFECTIVO -> "Efectivo"
        PaymentMethod.TRANSFERENCIA -> "Transferencia"
        PaymentMethod.TARJETA -> "Tarjeta"
    }

@Composable
fun PosScreen(viewModel: PosViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    // Venta completada: ticket
    state.lastSale?.let { sale ->
        TicketView(sale = sale, onNewSale = viewModel::onNewSale)
        return
    }

    if (state.bloqueadoSinTienda) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "No puedes vender sin una tienda asignada. " +
                    "Pide a un administrador que te asigne una.",
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(Modifier.fillMaxSize()) {

        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (state.isAdmin) {
                PosLocationSelector(state, viewModel::onLocationSelected)
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar producto o SKU") },
                singleLine = true,
                enabled = state.locationId != null,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isAdmin && state.locationId == null) {
                Text(
                    "Elige la ubicación desde la que vas a vender",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        LazyColumn(Modifier.weight(1f)) {
            // Resultados de búsqueda
            items(state.searchResults, key = { "s-${it.variantId}" }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAddItem(item) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item.portada?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp)),
                        )
                        Spacer(Modifier.padding(horizontal = 4.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(item.producto, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            listOf(item.sku, item.atributos).filter { it.isNotBlank() }
                                .joinToString("  ·  "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(moneda.format(item.precioLista))
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Agregar",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                HorizontalDivider()
            }

            // Carrito
            if (state.query.isBlank()) {
                items(state.cart.values.toList(), key = { "c-${it.item.variantId}" }) { entry ->
                    CartRow(
                        entry = entry,
                        onQuantityChange = { qty ->
                            viewModel.onQuantityChange(entry.item.variantId, qty)
                        },
                    )
                    HorizontalDivider()
                }
                if (state.cart.isEmpty()) {
                    item {
                        Text(
                            "Carrito vacío: busca un producto para agregarlo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        )
                    }
                }
            }
        }

        // Pie: total + cobrar
        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Total", style = MaterialTheme.typography.labelMedium)
                Text(
                    moneda.format(state.totalEstimado),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                onClick = viewModel::onOpenPayment,
                enabled = state.canCheckout,
            ) {
                if (state.isCheckingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Cobrar")
                }
            }
        }
    }

    if (state.showPaymentDialog) {
        PaymentDialog(
            total = state.totalEstimado,
            onDismiss = viewModel::onDismissPayment,
            onConfirm = viewModel::onCheckout,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PosLocationSelector(
    state: PosUiState,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = state.locations.firstOrNull { it.id == state.locationId }?.nombre ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vender desde") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.locations.forEach { location ->
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
private fun CartRow(entry: CartEntry, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(entry.item.producto, style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (entry.tienePromo) {
                    Text(
                        moneda.format(entry.item.precioLista),
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                }
                Text(
                    moneda.format(entry.precioUnitario),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.tienePromo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = { onQuantityChange(entry.cantidad - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Menos")
        }
        Text("${entry.cantidad}", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onQuantityChange(entry.cantidad + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = "Más")
        }
        Text(
            moneda.format(entry.importe),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDialog(
    total: Double,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, String?) -> Unit,
) {
    var metodo by remember { mutableStateOf(PaymentMethod.EFECTIVO) }
    var referencia by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cobrar ${moneda.format(total)}") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = metodo.etiqueta,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Método de pago") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        PaymentMethod.entries.forEach { pm ->
                            DropdownMenuItem(
                                text = { Text(pm.etiqueta) },
                                onClick = { metodo = pm; expanded = false },
                            )
                        }
                    }
                }
                if (metodo != PaymentMethod.EFECTIVO) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = referencia,
                        onValueChange = { referencia = it },
                        label = { Text("Referencia") },
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(metodo, referencia) }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun TicketView(sale: Order, onNewSale: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Venta completada", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Pedido #${sale.folio}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(sale.items, key = { it.id }) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("${item.cantidad} ×", Modifier.padding(end = 8.dp))
                    Text(
                        moneda.format(item.precioUnitario),
                        modifier = Modifier.weight(1f),
                    )
                    Text(moneda.format(item.cantidad * item.precioUnitario - item.descuento))
                }
            }
        }

        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(
                moneda.format(sale.total),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        sale.payments.firstOrNull()?.let { payment ->
            Text(
                "Pagado con ${payment.metodo.etiqueta}" +
                    (payment.referencia?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNewSale, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva venta")
        }
    }
}
