package com.passioagogo.market.ui.admin.catalog.attributes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

data class AttributePickerUiState(
    val presets: List<AttributePreset> = emptyList(),
    /** ids de los presets marcados. */
    val seleccionados: Set<String> = emptySet(),
    /**
     * Pares presentes en el atributo pero sin preset que los represente
     * (p. ej. capturados antes de existir las chips). Se conservan al guardar.
     */
    val extras: Map<String, String> = emptyMap(),
    val showForm: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AttributePickerViewModel @Inject constructor(
    private val repository: AttributePresetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttributePickerUiState())
    val uiState: StateFlow<AttributePickerUiState> = _uiState.asStateFlow()

    /** Carga los presets y marca los que ya están en [actuales]. */
    fun init(actuales: JsonObject) {
        viewModelScope.launch {
            when (val result = repository.getPresets()) {
                is DataResult.Success -> {
                    val presets = result.data
                    val pares = actuales.entries.associate { (k, v) ->
                        k to v.jsonPrimitive.content
                    }
                    val marcados = presets.filter { preset ->
                        pares[preset.clave]?.equals(preset.valor, ignoreCase = true) == true
                    }.map { it.id }.toSet()

                    // Lo que no corresponde a ninguna chip no se pierde
                    val cubiertas = presets.filter { it.id in marcados }.map { it.clave }.toSet()
                    val extras = pares.filterKeys { it !in cubiertas }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            presets = presets,
                            seleccionados = marcados,
                            extras = extras,
                        )
                    }
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    /**
     * Alterna una chip. Exclusión por clave: el jsonb solo admite un valor
     * por clave, así que marcar "Vidrio" desmarca "Silicona".
     */
    fun onToggle(preset: AttributePreset) = _uiState.update { state ->
        if (preset.id in state.seleccionados) {
            state.copy(seleccionados = state.seleccionados - preset.id)
        } else {
            val mismaClave = state.presets
                .filter { it.clave.equals(preset.clave, ignoreCase = true) }
                .map { it.id }
                .toSet()
            state.copy(
                seleccionados = state.seleccionados - mismaClave + preset.id,
                extras = state.extras - preset.clave,
            )
        }
    }

    fun onShowForm() = _uiState.update { it.copy(showForm = true, errorMessage = null) }
    fun onHideForm() = _uiState.update { it.copy(showForm = false) }

    fun onCreatePreset(draft: AttributePresetDraft) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.createPreset(draft)) {
                is DataResult.Success -> {
                    val nuevo = result.data
                    _uiState.update { state ->
                        val mismaClave = state.presets
                            .filter { it.clave.equals(nuevo.clave, ignoreCase = true) }
                            .map { it.id }
                            .toSet()
                        state.copy(
                            isSaving = false,
                            showForm = false,
                            presets = state.presets + nuevo,
                            // Recién creado se marca solo: es lo que buscaba
                            seleccionados = state.seleccionados - mismaClave + nuevo.id,
                            extras = state.extras - nuevo.clave,
                        )
                    }
                }
                is DataResult.Error -> _uiState.update {
                    val mensaje = result.error.toMessage()
                    it.copy(
                        isSaving = false,
                        errorMessage = if (mensaje.contains("uq_attribute_presets") ||
                            mensaje.contains("duplicate key")
                        ) "Ya existe una etiqueta con esa clave y valor" else mensaje,
                    )
                }
            }
        }
    }

    /** Construye el jsonb final: chips marcadas + pares no representados. */
    fun buildAttributes(): JsonObject {
        val state = _uiState.value
        val desdeChips = state.presets
            .filter { it.id in state.seleccionados }
            .associate { it.clave to JsonPrimitive(it.valor) }
        val desdeExtras = state.extras.mapValues { (_, v) -> JsonPrimitive(v) }
        return JsonObject(desdeExtras + desdeChips)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttributePickerDialog(
    actuales: JsonObject,
    onDismiss: () -> Unit,
    onConfirm: (JsonObject) -> Unit,
    viewModel: AttributePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.init(actuales) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atributos") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        state.presets.forEach { preset ->
                            FilterChip(
                                selected = preset.id in state.seleccionados,
                                onClick = { viewModel.onToggle(preset) },
                                label = { Text(preset.etiqueta) },
                            )
                        }
                        // Última chip: abre el mini formulario
                        AssistChip(
                            onClick = viewModel::onShowForm,
                            label = { Text("Agregar") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.width(18.dp),
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }

                    if (state.extras.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Sin etiqueta: " + state.extras.entries.joinToString(", ") {
                                "${it.key}: ${it.value}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (state.showForm) {
                        Spacer(Modifier.height(12.dp))
                        PresetForm(
                            isSaving = state.isSaving,
                            errorMessage = state.errorMessage,
                            onCancel = viewModel::onHideForm,
                            onSave = viewModel::onCreatePreset,
                        )
                    } else {
                        state.errorMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !state.isLoading,
                onClick = { onConfirm(viewModel.buildAttributes()) },
            ) { Text("Continuar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun PresetForm(
    isSaving: Boolean,
    errorMessage: String?,
    onCancel: () -> Unit,
    onSave: (AttributePresetDraft) -> Unit,
) {
    var identificador by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }

    val valido = identificador.isNotBlank() && clave.isNotBlank() && valor.isNotBlank()

    Column {
        Text("Nueva etiqueta", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
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
        errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            TextButton(
                enabled = valido && !isSaving,
                onClick = {
                    onSave(
                        AttributePresetDraft(
                            clave = clave,
                            valor = valor,
                            identificador = identificador,
                            emoji = emoji.ifBlank { null },
                        )
                    )
                },
            ) { Text(if (isSaving) "Guardando…" else "Crear etiqueta") }
        }
    }
}
