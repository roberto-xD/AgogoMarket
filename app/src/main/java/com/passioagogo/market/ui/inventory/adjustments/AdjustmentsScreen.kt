package com.passioagogo.market.ui.inventory.adjustments

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.inventory.AjusteInventario
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.inventory.MotivoAjuste
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.stock.etiqueta
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdjustmentsUiState(
    val esAdmin: Boolean = false,
    val miTienda: String? = null,
    val locations: List<Location> = emptyList(),
    /** null = todas (solo admin). */
    val locationId: String? = null,
    val ajustes: List<AjusteInventario> = emptyList(),
    /** Filtro por motivo; null = todos. */
    val motivo: MotivoAjuste? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val visibles: List<AjusteInventario>
        get() = motivo?.let { m -> ajustes.filter { it.motivo == m } } ?: ajustes

    /** Suma neta del periodo listado: indica si se infló o se recortó. */
    val neto: Int get() = visibles.sumOf { it.diferencia }
}

@HiltViewModel
class AdjustmentsViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdjustmentsUiState())
    val uiState: StateFlow<AdjustmentsUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value as? SessionState.Authenticated
        val esAdmin = sesion?.isAdmin == true
        val miTienda = sesion?.profile?.locationId
        _uiState.update {
            it.copy(
                esAdmin = esAdmin,
                miTienda = miTienda,
                // El vendedor solo ve lo suyo; RLS lo garantiza igualmente
                locationId = if (esAdmin) null else miTienda,
            )
        }
        viewModelScope.launch {
            if (esAdmin) {
                val locs = locationRepository.getLocations(includeInactive = true)
                if (locs is DataResult.Success) {
                    _uiState.update { it.copy(locations = locs.data) }
                }
            }
            cargar()
        }
    }

    fun onLocation(id: String?) {
        _uiState.update { it.copy(locationId = id) }
        refresh()
    }

    fun onMotivo(m: MotivoAjuste?) = _uiState.update { it.copy(motivo = m) }

    fun refresh() = viewModelScope.launch { cargar() }

    private suspend fun cargar() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = inventoryRepository.getAjustes(
            locationId = _uiState.value.locationId,
            limite = 200,
        )
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, ajustes = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentsScreen(viewModel: AdjustmentsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        if (state.esAdmin) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                OutlinedTextField(
                    value = state.locations.firstOrNull { it.id == state.locationId }?.nombre
                        ?: "Todas las ubicaciones",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ubicación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Todas las ubicaciones") },
                        onClick = { viewModel.onLocation(null); expanded = false },
                    )
                    state.locations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.nombre) },
                            onClick = { viewModel.onLocation(loc.id); expanded = false },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = state.motivo == null,
                onClick = { viewModel.onMotivo(null) },
                label = { Text("Todos") },
            )
            MotivoAjuste.entries.forEach { m ->
                FilterChip(
                    selected = state.motivo == m,
                    onClick = { viewModel.onMotivo(m) },
                    label = { Text(m.etiqueta) },
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${state.visibles.size} ajuste(s) · neto " +
                    (if (state.neto >= 0) "+${state.neto}" else "${state.neto}"),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        }

        when {
            state.isLoading && state.ajustes.isEmpty() ->
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

            state.visibles.isEmpty() -> Centrado {
                Text(
                    "Sin ajustes registrados",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyColumn {
                items(state.visibles, key = { it.id }) { ajuste ->
                    FilaAjuste(ajuste, mostrarUbicacion = state.locationId == null)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FilaAjuste(ajuste: AjusteInventario, mostrarUbicacion: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(ajuste.producto, style = MaterialTheme.typography.bodyLarge)
            Text(
                listOfNotNull(
                    ajuste.sku,
                    if (mostrarUbicacion) ajuste.ubicacion else null,
                    ajuste.createdAt?.take(10),
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                listOfNotNull(
                    ajuste.motivo.etiqueta,
                    ajuste.responsable,
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            ajuste.nota?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            // El salto importa más que el valor final: muestra la magnitud
            // del descuadre de un vistazo.
            Text(
                if (ajuste.diferencia >= 0) "+${ajuste.diferencia}"
                else "${ajuste.diferencia}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    ajuste.diferencia > 0 -> MaterialTheme.colorScheme.primary
                    ajuste.diferencia < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                "${ajuste.cantidadAnterior} → ${ajuste.cantidadNueva}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
