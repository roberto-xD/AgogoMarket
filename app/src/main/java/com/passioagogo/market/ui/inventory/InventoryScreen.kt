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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.inventory.StockItem
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.json.jsonPrimitive

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

@Composable
fun InventoryScreen(
    session: SessionState.Authenticated,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {

        // ---------- Filtros ----------
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (state.isAdmin) {
                LocationSelector(
                    state = state,
                    onSelected = viewModel::onLocationSelected,
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("Buscar producto, SKU o categoría") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                }
            }
        }

        // ---------- Resumen (solo admin: valor de inventario) ----------
        if (state.isAdmin && state.filteredItems.isNotEmpty()) {
            Text(
                text = "Valor de inventario: ${moneda.format(state.valorTotal)}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSelector(
    state: InventoryUiState,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = state.locations
        .firstOrNull { it.id == state.selectedLocationId }?.nombre
        ?: "Todas las ubicaciones"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
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
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todas las ubicaciones") },
                onClick = {
                    expanded = false
                    onSelected(null)
                },
            )
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
