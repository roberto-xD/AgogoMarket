package com.passioagogo.market.ui.admin.attributes

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.attributes.AttributePreset
import com.passioagogo.market.data.attributes.AttributePresetDraft
import com.passioagogo.market.data.attributes.AttributePresetRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PresetsUiState(
    val presets: List<AttributePreset> = emptyList(),
    val showInactive: Boolean = false,
    val editing: AttributePreset? = null,
    val creating: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val visible: List<AttributePreset>
        get() = if (showInactive) presets else presets.filter { it.activo }

    /** Agrupadas por clave: así se ve qué opciones compiten entre sí. */
    val porClave: List<Pair<String, List<AttributePreset>>>
        get() = visible.groupBy { it.clave }.toList().sortedBy { it.first }
}

@HiltViewModel
class PresetsViewModel @Inject constructor(
    private val repository: AttributePresetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PresetsUiState())
    val uiState: StateFlow<PresetsUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onToggleInactive(show: Boolean) = _uiState.update { it.copy(showInactive = show) }
    fun onEdit(preset: AttributePreset) = _uiState.update { it.copy(editing = preset) }
    fun onNew() = _uiState.update { it.copy(creating = true) }
    fun onDismiss() = _uiState.update { it.copy(editing = null, creating = false, errorMessage = null) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getPresets(includeInactive = true)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(isLoading = false, presets = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onCreate(draft: AttributePresetDraft) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = repository.createPreset(draft)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, creating = false) }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = traducir(r.error.toMessage()))
                }
            }
        }
    }

    fun onUpdate(preset: AttributePreset) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = repository.updatePreset(preset)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, editing = null) }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = traducir(r.error.toMessage()))
                }
            }
        }
    }

    /** Intercambia el orden con el vecino dentro de su misma clave. */
    fun onMove(preset: AttributePreset, arriba: Boolean) {
        val hermanos = _uiState.value.visible
            .filter { it.clave == preset.clave }
            .sortedBy { it.orden }
        val idx = hermanos.indexOfFirst { it.id == preset.id }
        val vecino = hermanos.getOrNull(if (arriba) idx - 1 else idx + 1) ?: return
        viewModelScope.launch {
            repository.updatePreset(preset.copy(orden = vecino.orden))
            repository.updatePreset(vecino.copy(orden = preset.orden))
            refresh()
        }
    }

    private fun traducir(mensaje: String) =
        if (mensaje.contains("uq_attribute_presets") || mensaje.contains("duplicate key")) {
            "Ya existe una etiqueta con esa clave y valor"
        } else {
            mensaje
        }
}

@Composable
fun AttributePresetsScreen(viewModel: PresetsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Etiquetas para capturar atributos de productos y variantes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.showInactive,
                    onClick = { viewModel.onToggleInactive(!state.showInactive) },
                    label = { Text("Inactivas") },
                )
            }

            state.errorMessage?.takeIf { state.editing == null && !state.creating }?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            when {
                state.isLoading && state.presets.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

                state.visible.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    Alignment.Center,
                ) {
                    Text(
                        "Sin etiquetas. Crea la primera con el botón +",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn {
                    state.porClave.forEach { (clave, presets) ->
                        item(key = "h-$clave") {
                            Text(
                                clave,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp,
                                ),
                            )
                        }
                        items(presets.sortedBy { it.orden }, key = { it.id }) { preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onEdit(preset) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (!preset.activo) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Inactiva",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.width(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        preset.etiqueta,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        "${preset.clave}: ${preset.valor}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { viewModel.onMove(preset, arriba = true) }) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Subir")
                                }
                                IconButton(onClick = { viewModel.onMove(preset, arriba = false) }) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Bajar")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::onNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva etiqueta")
        }
    }

    if (state.creating) {
        PresetDialog(
            editing = null,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = viewModel::onDismiss,
            onSaveNew = viewModel::onCreate,
            onSaveEdit = {},
        )
    }
    state.editing?.let { preset ->
        PresetDialog(
            editing = preset,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = viewModel::onDismiss,
            onSaveNew = {},
            onSaveEdit = viewModel::onUpdate,
        )
    }
}

@Composable
private fun PresetDialog(
    editing: AttributePreset?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSaveNew: (AttributePresetDraft) -> Unit,
    onSaveEdit: (AttributePreset) -> Unit,
) {
    var emoji by remember { mutableStateOf(editing?.emoji ?: "") }
    var identificador by remember { mutableStateOf(editing?.identificador ?: "") }
    var clave by remember { mutableStateOf(editing?.clave ?: "") }
    var valor by remember { mutableStateOf(editing?.valor ?: "") }
    var activo by remember { mutableStateOf(editing?.activo ?: true) }

    val valido = identificador.isNotBlank() && clave.isNotBlank() && valor.isNotBlank()
    val cambioParClave = editing != null &&
        (clave.trim().lowercase() != editing.clave || valor.trim() != editing.valor)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Nueva etiqueta" else "Editar etiqueta") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { if (it.length <= 4) emoji = it },
                        label = { Text("Emoji") },
                        singleLine = true,
                        modifier = Modifier.width(96.dp),
                    )
                    OutlinedTextField(
                        value = identificador,
                        onValueChange = { identificador = it },
                        label = { Text("Identificador") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Clave") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    "Se guardará como \"${clave.trim().lowercase().ifBlank { "clave" }}\": " +
                        "\"${valor.trim().ifBlank { "valor" }}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (cambioParClave) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Los productos guardados con el par anterior lo conservan: " +
                            "esta etiqueta dejará de reconocerlos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (editing != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = activo, onCheckedChange = { activo = it })
                        Text("Activa")
                    }
                }
                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !isSaving,
                onClick = {
                    if (editing == null) {
                        onSaveNew(
                            AttributePresetDraft(
                                clave = clave,
                                valor = valor,
                                identificador = identificador,
                                emoji = emoji.ifBlank { null },
                            )
                        )
                    } else {
                        onSaveEdit(
                            editing.copy(
                                clave = clave.trim().lowercase(),
                                valor = valor.trim(),
                                identificador = identificador.trim(),
                                emoji = emoji.ifBlank { null },
                                activo = activo,
                            )
                        )
                    }
                },
            ) { Text(if (isSaving) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
