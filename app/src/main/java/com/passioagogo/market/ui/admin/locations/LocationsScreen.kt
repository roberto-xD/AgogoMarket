package com.passioagogo.market.ui.admin.locations

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.LocationType
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationDraft
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal val LocationType.etiqueta: String
    get() = when (this) {
        LocationType.TIENDA -> "Tienda"
        LocationType.BODEGA -> "Bodega"
        LocationType.ONLINE -> "Online"
    }

data class LocationsUiState(
    val locations: List<Location> = emptyList(),
    val editing: Location? = null,
    val showDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationsUiState())
    val uiState: StateFlow<LocationsUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onNew() = _uiState.update { it.copy(showDialog = true, editing = null) }
    fun onEdit(location: Location) =
        _uiState.update { it.copy(showDialog = true, editing = location) }
    fun onDismiss() = _uiState.update { it.copy(showDialog = false, editing = null) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = locationRepository.getLocations(includeInactive = true)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success ->
                        state.copy(isLoading = false, locations = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onSave(nombre: String, direccion: String?, tipo: LocationType, activo: Boolean) {
        val editing = _uiState.value.editing
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (editing == null) {
                locationRepository.createLocation(
                    LocationDraft(nombre = nombre, direccion = direccion, tipo = tipo)
                )
            } else {
                locationRepository.updateLocation(
                    editing.copy(
                        nombre = nombre,
                        direccion = direccion,
                        tipo = tipo,
                        activo = activo,
                    )
                )
            }
            when (result) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(isSaving = false, showDialog = false, editing = null)
                    }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun LocationsScreen(viewModel: LocationsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
            LazyColumn {
                items(state.locations, key = { it.id }) { location ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEdit(location) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(location.nombre, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOfNotNull(
                                    location.tipo.etiqueta,
                                    location.direccion,
                                ).joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (!location.activo) {
                            Text(
                                "Inactiva",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::onNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva ubicación")
        }
    }

    if (state.showDialog) {
        LocationDialog(
            editing = state.editing,
            isSaving = state.isSaving,
            onDismiss = viewModel::onDismiss,
            onSave = viewModel::onSave,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDialog(
    editing: Location?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String?, LocationType, Boolean) -> Unit,
) {
    var nombre by remember { mutableStateOf(editing?.nombre ?: "") }
    var direccion by remember { mutableStateOf(editing?.direccion ?: "") }
    var tipo by remember { mutableStateOf(editing?.tipo ?: LocationType.TIENDA) }
    var activo by remember { mutableStateOf(editing?.activo ?: true) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Nueva ubicación" else "Editar ubicación") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = tipo.etiqueta,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        LocationType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.etiqueta) },
                                onClick = { tipo = t; expanded = false },
                            )
                        }
                    }
                }
                if (editing != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = activo, onCheckedChange = { activo = it })
                        Text("Activa")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && !isSaving,
                onClick = { onSave(nombre.trim(), direccion.ifBlank { null }, tipo, activo) },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
