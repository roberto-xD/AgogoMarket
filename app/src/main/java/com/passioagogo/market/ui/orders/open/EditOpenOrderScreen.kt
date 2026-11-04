package com.passioagogo.market.ui.orders.open

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import com.passioagogo.market.ui.common.OpcionesTeclado
import java.text.NumberFormat
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

/**
 * Completa y confirma un pedido abierto.
 *
 * Mientras siga abierto se pueden añadir o quitar artículos sin cancelar
 * nada: es lo que evita tener que recapturar un pedido entero por un
 * producto olvidado.
 *
 * Confirmar descuenta stock. El pago va aparte, para que quepan los
 * pagos parciales y el saldo quede a la vista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOpenOrderScreen(
    onConfirmado: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: EditOpenOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) { onBack() }

    LaunchedEffect(state.confirmado) {
        if (state.confirmado) state.pedido?.id?.let(onConfirmado)
    }

    if (state.isLoading && state.pedido == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val pedido = state.pedido ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Pedido no encontrado") }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Pedido #${pedido.folio}",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            "Sin confirmar · no ha descontado stock",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        // ---- Cliente ----
        var clienteAbierto by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(clienteAbierto, { clienteAbierto = it }) {
            OutlinedTextField(
                value = state.customers.firstOrNull { it.id == pedido.customerId }
                    ?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cliente") },
                placeholder = { Text("Elige el cliente") },
                isError = pedido.customerId == null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(clienteAbierto) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(clienteAbierto, { clienteAbierto = false }) {
                state.customers.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.nombre) },
                        onClick = { viewModel.onCliente(c.id); clienteAbierto = false },
                    )
                }
            }
        }

        // ---- Dirección ----
        if (state.addresses.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            var dirAbierta by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(dirAbierta, { dirAbierta = it }) {
                OutlinedTextField(
                    value = state.addresses
                        .firstOrNull { it.id == pedido.shippingAddressId }
                        ?.let { "${it.calle}, ${it.ciudad}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dirección de envío") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dirAbierta) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(dirAbierta, { dirAbierta = false }) {
                    state.addresses.forEach { d ->
                        DropdownMenuItem(
                            text = { Text("${d.calle}, ${d.ciudad}") },
                            onClick = { viewModel.onDireccion(d.id); dirAbierta = false },
                        )
                    }
                }
            }
        }

        // ---- Agregar productos ----
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQuery,
            label = { Text("Agregar producto (nombre o SKU)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(Modifier.weight(1f)) {
            items(state.searchResults, key = { "s-${it.variantId}" }) { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAgregar(info.variantId) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(info.producto)
                        Text(
                            listOfNotNull(
                                info.detalle.ifBlank { info.sku },
                                "quedan ${state.disponible(info.variantId)}",
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
                items(state.items, key = { "i-${it.id}" }) { item ->
                    val info = state.variantIndex[item.variantId]
                    val falta = item.cantidad > state.disponible(item.variantId)
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(info?.producto ?: item.variantId)
                                Text(
                                    listOfNotNull(
                                        info?.detalle?.ifBlank { info.sku },
                                        "${item.cantidad} × " +
                                            moneda.format(item.precioUnitario),
                                    ).joinToString("  ·  "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                moneda.format(
                                    item.cantidad * item.precioUnitario - item.descuento,
                                ),
                                fontWeight = FontWeight.SemiBold,
                            )
                            IconButton(
                                onClick = { viewModel.onQuitar(item) },
                                enabled = !state.isSaving,
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                            }
                        }
                        if (falta) {
                            Text(
                                "Solo quedan ${state.disponible(item.variantId)} " +
                                    "en esta ubicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        OutlinedTextField(
            value = state.notas,
            onValueChange = viewModel::onNotas,
            label = { Text("Notas") },
            singleLine = true,
            keyboardOptions = OpcionesTeclado.Texto,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(moneda.format(pedido.total), fontWeight = FontWeight.Bold)
        }

        if (pedido.customerId == null) {
            Text(
                "Asigna un cliente para poder confirmar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(8.dp))
        var confirmando by remember { mutableStateOf(false) }
        Button(
            onClick = { confirmando = true },
            enabled = state.puedeConfirmar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSaving) "Guardando…" else "Confirmar (descuenta stock)")
        }

        if (confirmando) {
            AlertDialog(
                onDismissRequest = { confirmando = false },
                title = { Text("Confirmar el pedido") },
                text = {
                    Text(
                        "Se descontará el stock y la dirección quedará congelada. " +
                            "Después podrás registrar el pago, total o parcial.",
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        confirmando = false
                        viewModel.onGuardarNotas()
                        viewModel.onConfirmar()
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
                },
            )
        }
    }

    state.errorMessage?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarError,
            title = { Text("No se pudo completar") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarError) { Text("Entendido") }
            },
        )
    }
}
