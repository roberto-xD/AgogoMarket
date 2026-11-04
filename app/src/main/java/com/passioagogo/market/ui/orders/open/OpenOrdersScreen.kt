package com.passioagogo.market.ui.orders.open

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.sales.Order
import java.text.NumberFormat
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

/**
 * Pedidos de envío que todavía no se han confirmado.
 *
 * Existen en la base pero no han movido inventario: se pueden corregir,
 * completar y confirmar cuando estén listos. Son también los que se
 * quedan a medias, de ahí el borrado rápido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenOrdersScreen(
    onAbrirPedido: (String) -> Unit,
    onNuevoPedido: () -> Unit,
    onBack: () -> Unit,
    viewModel: OpenOrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) { onBack() }
    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${state.pedidos.size} pedido(s) sin confirmar",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "No han descontado stock. Desliza a la izquierda para " +
                            "descartar uno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = viewModel::onCambiarOrden) {
                    Icon(Icons.Filled.SwapVert, contentDescription = null)
                    Text(if (state.masNuevosPrimero) " Recientes" else " Antiguos")
                }
            }

            when {
                state.isLoading && state.pedidos.isEmpty() ->
                    Centrado { CircularProgressIndicator() }

                state.pedidos.isEmpty() -> Centrado {
                    Text(
                        "No hay pedidos sin confirmar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn {
                    items(state.pedidos, key = { it.id }) { pedido ->
                        val dismiss = rememberSwipeToDismissBoxState(
                            confirmValueChange = { valor ->
                                if (valor == SwipeToDismissBoxValue.EndToStart) {
                                    // Se pide confirmación antes de borrar:
                                    // un deslizamiento accidental no debe
                                    // tirar un pedido a medio capturar.
                                    viewModel.onPedirDescartar(pedido)
                                }
                                false
                            },
                        )
                        SwipeToDismissBox(
                            state = dismiss,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Descartar",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            },
                        ) {
                            FilaPedido(
                                pedido = pedido,
                                nombreCliente = state.nombreCliente(pedido.customerId),
                                onClick = { onAbrirPedido(pedido.id) },
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNuevoPedido,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nuevo pedido")
        }
    }

    state.porDescartar?.let { pedido ->
        AlertDialog(
            onDismissRequest = viewModel::onCancelarDescartar,
            title = { Text("Descartar el pedido") },
            text = {
                Text(
                    "El pedido #${pedido.folio} se cancelará. No ha descontado " +
                        "stock, así que no hay nada que devolver, pero perderás " +
                        "lo capturado.",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onDescartar) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelarDescartar) {
                    Text("Conservar")
                }
            },
        )
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

@Composable
private fun FilaPedido(
    pedido: Order,
    nombreCliente: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "#${pedido.folio}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                // Sin cliente todavía es normal: se asigna después
                nombreCliente ?: "Sin cliente asignado",
                style = MaterialTheme.typography.bodySmall,
                color = if (nombreCliente == null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                pedido.createdAt?.take(10).orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            moneda.format(pedido.total),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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
