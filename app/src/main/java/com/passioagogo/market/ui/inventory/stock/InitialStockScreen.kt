package com.passioagogo.market.ui.inventory.stock

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.preferences.UserPreferences
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.transfers.VariantInfo
import com.passioagogo.market.ui.inventory.transfers.variantIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InitialStockUiState(
    val locations: List<Location> = emptyList(),
    val locationId: String? = null,
    val query: String = "",
    val catalog: List<VariantInfo> = emptyList(),
    /** Existencias actuales en la ubicación elegida. */
    val actuales: Map<String, Int> = emptyMap(),
    /** Cantidades escritas por el usuario, sin guardar aún. */
    val edits: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showConfirm: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    fun cantidadMostrada(variantId: String): String =
        edits[variantId] ?: (actuales[variantId] ?: 0).toString()

    /** Solo se escriben las variantes cuya cantidad cambió y es válida. */
    val cambios: Map<String, Int>
        get() = edits.mapNotNull { (variantId, texto) ->
            val valor = texto.toIntOrNull()
            if (valor == null || valor < 0) null
            else if (valor == (actuales[variantId] ?: 0)) null
            else variantId to valor
        }.toMap()

    val hayInvalidos: Boolean
        get() = edits.values.any { it.isNotBlank() && (it.toIntOrNull()?.let { v -> v < 0 } ?: true) }

    val visibles: List<VariantInfo>
        get() {
            if (query.isBlank()) return catalog
            val q = query.trim().lowercase()
            return catalog.filter {
                it.producto.lowercase().contains(q) || it.sku.lowercase().contains(q)
            }
        }

    val canSave: Boolean
        get() = !isSaving && locationId != null && cambios.isNotEmpty() && !hayInvalidos
}

@HiltViewModel
class InitialStockViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InitialStockUiState())
    val uiState: StateFlow<InitialStockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val locs = locationRepository.getLocations()
            val index = catalogRepository.variantIndex()
            val guardada = userPreferences.activeLocationId.first()
            val lista = (locs as? DataResult.Success)?.data ?: emptyList()
            val inicial = guardada?.takeIf { id -> lista.any { it.id == id } }
                ?: lista.firstOrNull()?.id

            _uiState.update {
                it.copy(
                    isLoading = false,
                    locations = lista,
                    locationId = inicial,
                    catalog = index.values.sortedBy { v -> v.producto },
                    errorMessage = (locs as? DataResult.Error)?.error?.toMessage(),
                )
            }
            inicial?.let { cargarExistencias(it) }
        }
    }

    fun onLocationSelected(id: String) {
        // Cambiar de ubicación descarta lo escrito: las cantidades pertenecen
        // a la ubicación en la que se capturaron.
        _uiState.update { it.copy(locationId = id, edits = emptyMap(), successMessage = null) }
        viewModelScope.launch { cargarExistencias(id) }
    }

    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }

    fun onCantidadChange(variantId: String, texto: String) = _uiState.update {
        it.copy(edits = it.edits + (variantId to texto), successMessage = null)
    }

    fun onAskSave() = _uiState.update { it.copy(showConfirm = true) }
    fun onDismissConfirm() = _uiState.update { it.copy(showConfirm = false) }

    fun onSave() {
        val state = _uiState.value
        val locationId = state.locationId ?: return
        val cambios = state.cambios
        if (cambios.isEmpty()) return

        _uiState.update { it.copy(isSaving = true, showConfirm = false, errorMessage = null) }
        viewModelScope.launch {
            var guardados = 0
            var error: String? = null
            for ((variantId, cantidad) in cambios) {
                when (val r = inventoryRepository.setStock(variantId, locationId, cantidad)) {
                    is DataResult.Success -> guardados++
                    is DataResult.Error -> {
                        error = r.error.toMessage()
                        break
                    }
                }
            }
            // Se recargan las existencias reales: si algo falló a medias, la
            // pantalla refleja lo que quedó en el servidor, no lo que se pidió.
            cargarExistencias(locationId)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    edits = emptyMap(),
                    errorMessage = error,
                    successMessage = if (guardados > 0) {
                        "$guardados producto(s) actualizados"
                    } else null,
                )
            }
        }
    }

    private suspend fun cargarExistencias(locationId: String) {
        when (val r = inventoryRepository.getStock(locationId = locationId)) {
            is DataResult.Success -> _uiState.update { state ->
                state.copy(actuales = r.data.associate { it.variantId to it.cantidad })
            }
            is DataResult.Error -> _uiState.update {
                it.copy(errorMessage = r.error.toMessage())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialStockScreen(viewModel: InitialStockViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            var expanded by remember { mutableStateOf(false) }
            val nombre = state.locations.firstOrNull { it.id == state.locationId }?.nombre ?: ""
            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ubicación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
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
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar producto o SKU") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Escribe la existencia real de cada producto. El valor " +
                    "reemplaza al actual, no se suma.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (state.visibles.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (state.query.isBlank()) "No hay productos en el catálogo"
                    else "Sin resultados para \"${state.query}\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(state.visibles, key = { it.variantId }) { info ->
                    val texto = state.cantidadMostrada(info.variantId)
                    val actual = state.actuales[info.variantId] ?: 0
                    val modificado = texto.toIntOrNull()?.let { it != actual } ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(info.producto, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOf(info.sku, "actual: $actual").joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { viewModel.onCantidadChange(info.variantId, it) },
                            singleLine = true,
                            isError = texto.toIntOrNull()?.let { it < 0 } ?: texto.isNotBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(96.dp),
                        )
                        if (modificado) {
                            Text(
                                "  •",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        state.successMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${state.cambios.size} cambio(s)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = viewModel::onAskSave, enabled = state.canSave) {
                Text(if (state.isSaving) "Guardando…" else "Guardar existencias")
            }
        }
    }

    if (state.showConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissConfirm,
            title = { Text("Actualizar existencias") },
            text = {
                Text(
                    "Se fijará la existencia de ${state.cambios.size} producto(s) en " +
                        "esta ubicación. Es un ajuste directo: no genera compra ni " +
                        "movimiento de inventario."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onSave) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissConfirm) { Text("Cancelar") }
            },
        )
    }
}
