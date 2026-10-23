package com.passioagogo.market.ui.inventory

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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.inventory.StockItem
import com.passioagogo.market.ui.common.SelectorUbicacion
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.json.jsonPrimitive

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

@Composable
fun InventoryScreen(
    onOpenStockTake: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {

        var mostrarResumen by remember { mutableStateOf(false) }

        // ---------- Filtros ----------
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (state.isAdmin) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SelectorUbicacion(
                        locations = state.locations,
                        locationId = state.selectedLocationId,
                        onSelected = viewModel::onLocationSelected,
                        opcionTodas = "Todas las ubicaciones",
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // También el vendedor, acotado a su tienda. Sin tienda no hay
            // existencias que ajustar.
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.isAdmin || !state.sinTienda) {
                    TextButton(onClick = onOpenStockTake) {
                        Icon(Icons.Filled.EditNote, contentDescription = null)
                        Text("  Registrar existencias")
                    }
                }
                Spacer(Modifier.weight(1f))
                if (state.filteredItems.isNotEmpty()) {
                    IconButton(onClick = { mostrarResumen = true }) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Resumen del inventario",
                        )
                    }
                }
                // Para el vendedor no hay dropdown arriba: el refresco va aquí
                if (!state.isAdmin) {
                    IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar producto, SKU o categoría") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (mostrarResumen) {
            ResumenInventarioSheet(
                state = state,
                onDismiss = { mostrarResumen = false },
            )
        }

        // ---------- Contenido ----------
        when {
            state.isLoading && state.items.isEmpty() -> Centered { CircularProgressIndicator() }

            state.errorMessage != null -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.errorMessage ?: "",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::refresh) { Text("Reintentar") }
                }
            }

            state.sinTienda -> Centered {
                Text(
                    "No tienes tienda asignada, por lo que no hay existencias " +
                        "que mostrar. Pide a un administrador que te asigne una.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            state.filteredItems.isEmpty() -> Centered {
                Text(
                    if (state.query.isBlank()) "Sin existencias registradas"
                    else "Sin resultados para \"${state.query}\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                items(state.filteredItems, key = { "${it.variantId}-${it.locationId}" }) { item ->
                    StockRow(item = item, showLocation = state.selectedLocationId == null)
                    HorizontalDivider()
                }
            }
        }
    }
}


@Composable
private fun StockRow(item: StockItem, showLocation: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.producto, style = MaterialTheme.typography.bodyLarge)
            val variante = item.variante.entries
                .joinToString(" · ") { (k, v) -> "$k: ${v.jsonPrimitive.content}" }
            Text(
                text = listOfNotNull(
                    item.sku,
                    variante.ifBlank { null },
                    if (showLocation) item.tienda else null,
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${item.cantidad}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (item.cantidad == 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
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


/**
 * Resumen económico del inventario visible.
 *
 * Se calcula sobre lo filtrado, no sobre todo el catálogo: si hay una
 * búsqueda o una ubicación seleccionada, los totales corresponden a eso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResumenInventarioSheet(
    state: InventoryUiState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Resumen del inventario", style = MaterialTheme.typography.titleLarge)
            Text(
                "${state.unidadesTotales} unidades en ${state.filteredItems.size} variante(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            // Qué entra en el cálculo. Mirando solo los números no hay forma
            // de saber que responden al filtro activo; con una búsqueda
            // puesta, las cifras son de un subconjunto.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.query.isBlank()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                ),
            ) {
                Text(
                    state.alcanceResumen,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.query.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    },
                    modifier = Modifier.padding(12.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            CifraResumen(
                etiqueta = "Costo del inventario",
                monto = state.costoTotal,
                explicacion = "Lo que costó la mercancía que hoy tienes en existencia, " +
                    "según el costo de cada variante. Es el dinero que ya está " +
                    "invertido en el almacén.",
            )

            Spacer(Modifier.height(16.dp))
            CifraResumen(
                etiqueta = "Valor de venta",
                monto = state.valorVentaTotal,
                destacado = true,
                explicacion = "Lo que ingresarías si vendieras todo a precio de lista. " +
                    "No descuenta promociones ni rebajas de mostrador, así que es " +
                    "un techo y no una previsión.",
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Margen potencial",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "Diferencia entre ambas cifras. Es una ganancia posible, " +
                            "no realizada: depende de vender todo y sin descuentos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        moneda.format(state.margenPotencial),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "%.1f%%".format(state.margenPorcentaje),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Sin costo capturado el margen sale inflado: conviene avisarlo
            // antes de que alguien tome una decisión con esa cifra.
            if (state.sinCosto > 0) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        "${state.sinCosto} variante(s) no tienen costo capturado, " +
                            "así que el costo real es mayor y el margen aparece " +
                            "más alto de lo que es.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CifraResumen(
    etiqueta: String,
    monto: Double,
    explicacion: String,
    destacado: Boolean = false,
) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        Text(
            moneda.format(monto),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (destacado) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            explicacion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
