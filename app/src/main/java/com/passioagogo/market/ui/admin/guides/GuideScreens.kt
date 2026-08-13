package com.passioagogo.market.ui.admin.guides

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.guides.Guide
import com.passioagogo.market.data.guides.GuideDraft
import com.passioagogo.market.data.guides.GuideRepository
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.catalog.Category
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============ Lista ============

data class GuidesUiState(
    val guides: List<Guide> = emptyList(),
    val showInactive: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val visible: List<Guide>
        get() = if (showInactive) guides else guides.filter { it.activo }
}

@HiltViewModel
class GuidesViewModel @Inject constructor(
    private val repository: GuideRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuidesUiState())
    val uiState: StateFlow<GuidesUiState> = _uiState.asStateFlow()

    fun onToggleInactive(show: Boolean) = _uiState.update { it.copy(showInactive = show) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getGuides()
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(isLoading = false, guides = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun GuidesListScreen(
    onOpenGuide: (String) -> Unit,
    onNewGuide: () -> Unit,
    viewModel: GuidesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Contenido de la sección «Uso y cuidados» de la web",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.showInactive,
                    onClick = { viewModel.onToggleInactive(!state.showInactive) },
                    label = { Text("Borradores") },
                )
            }

            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            when {
                state.isLoading && state.guides.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

                state.visible.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    Alignment.Center,
                ) {
                    Text(
                        "Sin guías publicadas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn {
                    items(state.visible, key = { it.id }) { guide ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenGuide(guide.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!guide.activo) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = "Borrador",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    guide.etiqueta,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    guide.resumen ?: "Orden ${guide.orden}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                            if (guide.advertencias != null) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = "Con advertencias",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewGuide,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva guía")
        }
    }
}

// ============ Editor ============

data class GuideEditUiState(
    val guideId: String? = null,
    val titulo: String = "",
    val emoji: String = "",
    val resumen: String = "",
    val uso: String = "",
    val limpieza: String = "",
    val cuidados: String = "",
    val advertencias: String = "",
    val categoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val orden: String = "0",
    val activo: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
) {
    val isNew: Boolean get() = guideId == null

    /** Réplica del CHECK guides_con_contenido. */
    val tieneContenido: Boolean
        get() = uso.isNotBlank() || limpieza.isNotBlank() || cuidados.isNotBlank()

    val canSave: Boolean
        get() = !isSaving && titulo.isNotBlank() && tieneContenido &&
            orden.toIntOrNull() != null && resumen.length <= 200
}

@HiltViewModel
class GuideEditViewModel @Inject constructor(
    private val repository: GuideRepository,
    private val catalogRepository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialId: String? = savedStateHandle["guideId"]

    private val _uiState = MutableStateFlow(GuideEditUiState())
    val uiState: StateFlow<GuideEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            catalogRepository.observeCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            if (initialId == null) {
                _uiState.update { it.copy(isLoading = false) }
            } else {
                val result = repository.getGuides()
                val guide = (result as? DataResult.Success)?.data?.firstOrNull { it.id == initialId }
                if (guide == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Guía no encontrada")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            guideId = guide.id,
                            titulo = guide.titulo,
                            emoji = guide.emoji.orEmpty(),
                            resumen = guide.resumen.orEmpty(),
                            uso = guide.uso.orEmpty(),
                            limpieza = guide.limpieza.orEmpty(),
                            cuidados = guide.cuidados.orEmpty(),
                            advertencias = guide.advertencias.orEmpty(),
                            categoryId = guide.categoryId,
                            orden = guide.orden.toString(),
                            activo = guide.activo,
                        )
                    }
                }
            }
        }
    }

    fun onTitulo(v: String) = _uiState.update { it.copy(titulo = v) }
    fun onEmoji(v: String) = _uiState.update { if (v.length <= 4) it.copy(emoji = v) else it }
    fun onResumen(v: String) = _uiState.update { it.copy(resumen = v) }
    fun onUso(v: String) = _uiState.update { it.copy(uso = v) }
    fun onLimpieza(v: String) = _uiState.update { it.copy(limpieza = v) }
    fun onCuidados(v: String) = _uiState.update { it.copy(cuidados = v) }
    fun onAdvertencias(v: String) = _uiState.update { it.copy(advertencias = v) }
    fun onCategoria(v: String?) = _uiState.update { it.copy(categoryId = v) }
    fun onOrden(v: String) = _uiState.update { it.copy(orden = v) }
    fun onActivo(v: Boolean) = _uiState.update { it.copy(activo = v) }
    fun onAskDelete() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun onDismissDelete() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun onDelete() {
        val id = _uiState.value.guideId ?: return
        _uiState.update { it.copy(isSaving = true, showDeleteConfirm = false) }
        viewModelScope.launch {
            when (val r = repository.deleteGuide(id)) {
                is DataResult.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (state.isNew) {
                repository.createGuide(
                    GuideDraft(
                        titulo = state.titulo.trim(),
                        resumen = state.resumen.trim().ifBlank { null },
                        emoji = state.emoji.trim().ifBlank { null },
                        uso = state.uso.ifBlank { null },
                        limpieza = state.limpieza.ifBlank { null },
                        cuidados = state.cuidados.ifBlank { null },
                        advertencias = state.advertencias.ifBlank { null },
                        categoryId = state.categoryId,
                        orden = state.orden.toInt(),
                    )
                )
            } else {
                repository.updateGuide(
                    Guide(
                        id = state.guideId!!,
                        titulo = state.titulo.trim(),
                        resumen = state.resumen.trim().ifBlank { null },
                        emoji = state.emoji.trim().ifBlank { null },
                        uso = state.uso.ifBlank { null },
                        limpieza = state.limpieza.ifBlank { null },
                        cuidados = state.cuidados.ifBlank { null },
                        advertencias = state.advertencias.ifBlank { null },
                        categoryId = state.categoryId,
                        orden = state.orden.toInt(),
                        activo = state.activo,
                    )
                )
            }
            _uiState.update {
                when (result) {
                    is DataResult.Success -> it.copy(isSaving = false, saved = true)
                    is DataResult.Error ->
                        it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideEditScreen(
    onSaved: () -> Unit,
    viewModel: GuideEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.emoji,
                onValueChange = viewModel::onEmoji,
                label = { Text("Emoji") },
                singleLine = true,
                modifier = Modifier.width(96.dp),
            )
            OutlinedTextField(
                value = state.titulo,
                onValueChange = viewModel::onTitulo,
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.resumen,
            onValueChange = viewModel::onResumen,
            label = { Text("Resumen (se lee con la guía plegada)") },
            supportingText = { Text("${state.resumen.length}/200") },
            isError = state.resumen.length > 200,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
        Text("Contenido", style = MaterialTheme.typography.labelLarge)
        if (!state.tieneContenido) {
            Text(
                "Completa al menos uno: uso, limpieza o cuidados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.uso,
            onValueChange = viewModel::onUso,
            label = { Text("Uso") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.limpieza,
            onValueChange = viewModel::onLimpieza,
            label = { Text("Limpieza") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.cuidados,
            onValueChange = viewModel::onCuidados,
            label = { Text("Cuidados y conservación") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.advertencias,
            onValueChange = viewModel::onAdvertencias,
            label = { Text("Advertencias (se muestran destacadas)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded, { expanded = it }) {
            OutlinedTextField(
                value = state.categories.firstOrNull { it.id == state.categoryId }?.nombre
                    ?: "Sin categoría",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría del catálogo (opcional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Sin categoría") },
                    onClick = { viewModel.onCategoria(null); expanded = false },
                )
                state.categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.nombre) },
                        onClick = { viewModel.onCategoria(cat.id); expanded = false },
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.orden,
            onValueChange = viewModel::onOrden,
            label = { Text("Orden") },
            singleLine = true,
            isError = state.orden.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        if (!state.isNew) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = state.activo, onCheckedChange = viewModel::onActivo)
                Text("  Publicada en la web")
            }
        }

        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                when {
                    state.isSaving -> "Guardando…"
                    state.isNew -> "Crear guía"
                    else -> "Guardar cambios"
                }
            )
        }
        if (!state.isNew) {
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = viewModel::onAskDelete,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text("  Eliminar guía")
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDelete,
            title = { Text("Eliminar guía") },
            text = { Text("Se quitará de la web de forma permanente.") },
            confirmButton = {
                TextButton(onClick = viewModel::onDelete) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDelete) { Text("Cancelar") }
            },
        )
    }
}
