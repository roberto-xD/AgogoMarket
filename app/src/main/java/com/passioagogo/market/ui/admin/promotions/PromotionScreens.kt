package com.passioagogo.market.ui.admin.promotions

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.catalog.Category
import com.passioagogo.market.domain.common.PromotionType
import com.passioagogo.market.domain.promotions.Promotion
import com.passioagogo.market.domain.promotions.PromotionDraft
import com.passioagogo.market.domain.promotions.PromotionRepository
import com.passioagogo.market.domain.promotions.PromotionTarget
import com.passioagogo.market.domain.promotions.TargetDraft
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.transfers.VariantInfo
import com.passioagogo.market.ui.inventory.transfers.variantIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

internal val PromotionType.etiqueta: String
    get() = when (this) {
        PromotionType.PORCENTAJE -> "Porcentaje"
        PromotionType.MONTO_FIJO -> "Monto fijo"
        PromotionType.PRECIO_ESPECIAL -> "Precio especial"
    }

internal fun Promotion.valorTexto(): String = when (tipo) {
    PromotionType.PORCENTAJE -> "${valor.toInt()}%"
    PromotionType.MONTO_FIJO -> "-${moneda.format(valor)}"
    PromotionType.PRECIO_ESPECIAL -> moneda.format(valor)
}

// Fechas: la UI maneja "yyyy-MM-dd"; al guardar se convierte a ISO
// (fin de día inclusivo para fecha_fin).
private val diaFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

internal fun String.isoADia(): String = take(10)
internal fun millisADia(millis: Long): String = diaFmt.format(Date(millis))
internal fun String.diaAIsoInicio(): String = "${this}T00:00:00Z"
internal fun String.diaAIsoFin(): String = "${this}T23:59:59Z"

// ============ Lista ============

data class PromotionsUiState(
    val activeOnly: Boolean = true,
    val promotions: List<Promotion> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val visible: List<Promotion>
        get() = if (activeOnly) promotions.filter { it.activo } else promotions
}

@HiltViewModel
class PromotionsViewModel @Inject constructor(
    private val promotionRepository: PromotionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState: StateFlow<PromotionsUiState> = _uiState.asStateFlow()

    fun onToggleActiveOnly(activeOnly: Boolean) =
        _uiState.update { it.copy(activeOnly = activeOnly) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = promotionRepository.getPromotions(includeInactive = true)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success ->
                        state.copy(isLoading = false, promotions = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

// ============ Edición / creación ============

data class TargetDisplay(val target: PromotionTarget, val nombre: String)

data class PromotionEditUiState(
    val promotionId: String? = null,
    val nombre: String = "",
    val tipo: PromotionType = PromotionType.PORCENTAJE,
    val valor: String = "",
    /** yyyy-MM-dd */
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val notas: String = "",
    val activo: Boolean = true,
    /** Targets del borrador (alta) o ya persistidos (edición). */
    val draftTargets: List<TargetDraft> = emptyList(),
    val savedTargets: List<TargetDisplay> = emptyList(),
    val categories: List<Category> = emptyList(),
    val productNames: Map<String, String> = emptyMap(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val showTargetDialog: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
) {
    val isNew: Boolean get() = promotionId == null

    val canSave: Boolean
        get() = !isSaving && nombre.isNotBlank() &&
            valor.toDoubleOrNull()?.let { v ->
                v > 0 && (tipo != PromotionType.PORCENTAJE || v <= 100)
            } == true &&
            fechaInicio.isNotBlank() && fechaFin.isNotBlank() &&
            fechaFin >= fechaInicio &&
            (!isNew || draftTargets.isNotEmpty())

    fun targetNombre(t: TargetDraft): String = when {
        t.categoryId != null ->
            "Categoría: " + (categories.firstOrNull { it.id == t.categoryId }?.nombre ?: "…")
        t.productId != null ->
            "Producto: " + (productNames[t.productId] ?: "…")
        else ->
            "Variante: " + (variantIndex[t.variantId]?.sku ?: "…")
    }
}

@HiltViewModel
class PromotionEditViewModel @Inject constructor(
    private val promotionRepository: PromotionRepository,
    private val catalogRepository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialId: String? = savedStateHandle["promotionId"]

    private val _uiState = MutableStateFlow(PromotionEditUiState())
    val uiState: StateFlow<PromotionEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val categories = catalogRepository.observeCategories().first()
            val products = catalogRepository.observeProducts().first()
            val index = catalogRepository.variantIndex()
            _uiState.update {
                it.copy(
                    categories = categories,
                    productNames = products.associate { pw -> pw.product.id to pw.product.nombre },
                    variantIndex = index,
                )
            }
            if (initialId != null) loadPromotion(initialId)
            else _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadPromotion(id: String) {
        val result = promotionRepository.getPromotions(includeInactive = true)
        val promo = (result as? DataResult.Success)?.data?.firstOrNull { it.id == id }
        if (promo == null) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Promoción no encontrada")
            }
            return
        }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                promotionId = promo.id,
                nombre = promo.nombre,
                tipo = promo.tipo,
                valor = promo.valor.toString(),
                fechaInicio = promo.fechaInicio.isoADia(),
                fechaFin = promo.fechaFin.isoADia(),
                notas = promo.notas.orEmpty(),
                activo = promo.activo,
                savedTargets = promo.targets.map { t ->
                    TargetDisplay(
                        target = t,
                        nombre = state.targetNombre(
                            TargetDraft(t.categoryId, t.productId, t.variantId)
                        ),
                    )
                },
            )
        }
    }

    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v) }
    fun onTipoChange(v: PromotionType) = _uiState.update { it.copy(tipo = v) }
    fun onValorChange(v: String) = _uiState.update { it.copy(valor = v) }
    fun onFechaInicio(dia: String) = _uiState.update { it.copy(fechaInicio = dia) }
    fun onFechaFin(dia: String) = _uiState.update { it.copy(fechaFin = dia) }
    fun onNotasChange(v: String) = _uiState.update { it.copy(notas = v) }
    fun onActivoChange(v: Boolean) = _uiState.update { it.copy(activo = v) }
    fun onOpenTargetDialog() = _uiState.update { it.copy(showTargetDialog = true) }
    fun onDismissTargetDialog() = _uiState.update { it.copy(showTargetDialog = false) }

    fun onAddTarget(target: TargetDraft) {
        val state = _uiState.value
        _uiState.update { it.copy(showTargetDialog = false) }
        if (state.isNew) {
            _uiState.update { it.copy(draftTargets = it.draftTargets + target) }
        } else {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            viewModelScope.launch {
                when (val r = promotionRepository.addTarget(state.promotionId!!, target)) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(isSaving = false) }
                        loadPromotion(state.promotionId)
                    }
                    is DataResult.Error -> _uiState.update {
                        it.copy(isSaving = false, errorMessage = r.error.toMessage())
                    }
                }
            }
        }
    }

    fun onRemoveDraftTarget(target: TargetDraft) =
        _uiState.update { it.copy(draftTargets = it.draftTargets - target) }

    fun onRemoveSavedTarget(targetId: String) {
        val promoId = _uiState.value.promotionId ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = promotionRepository.removeTarget(targetId)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    loadPromotion(promoId)
                }
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
                promotionRepository.createPromotion(
                    PromotionDraft(
                        nombre = state.nombre.trim(),
                        tipo = state.tipo,
                        valor = state.valor.toDouble(),
                        fechaInicio = state.fechaInicio.diaAIsoInicio(),
                        fechaFin = state.fechaFin.diaAIsoFin(),
                        notas = state.notas.ifBlank { null },
                        targets = state.draftTargets,
                    )
                )
            } else {
                promotionRepository.updatePromotion(
                    Promotion(
                        id = state.promotionId!!,
                        nombre = state.nombre.trim(),
                        tipo = state.tipo,
                        valor = state.valor.toDouble(),
                        fechaInicio = state.fechaInicio.diaAIsoInicio(),
                        fechaFin = state.fechaFin.diaAIsoFin(),
                        activo = state.activo,
                        notas = state.notas.ifBlank { null },
                        targets = emptyList(),
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

// ============ Pantallas ============

@Composable
fun PromotionsListScreen(
    onOpenPromotion: (String) -> Unit,
    onNewPromotion: () -> Unit,
    viewModel: PromotionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.activeOnly,
                    onClick = { viewModel.onToggleActiveOnly(true) },
                    label = { Text("Activas") },
                )
                FilterChip(
                    selected = !state.activeOnly,
                    onClick = { viewModel.onToggleActiveOnly(false) },
                    label = { Text("Todas") },
                )
            }

            when {
                state.isLoading && state.promotions.isEmpty() ->
                    CenteredBox { CircularProgressIndicator() }

                state.errorMessage != null -> CenteredBox {
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

                state.visible.isEmpty() -> CenteredBox {
                    Text("Sin promociones", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                else -> LazyColumn {
                    items(state.visible, key = { it.id }) { promo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPromotion(promo.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(promo.nombre, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${promo.valorTexto()}  ·  " +
                                        "${promo.fechaInicio.isoADia()} → ${promo.fechaFin.isoADia()}" +
                                        "  ·  ${promo.targets.size} objetivo(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            AssistChip(
                                onClick = { onOpenPromotion(promo.id) },
                                label = { Text(if (promo.activo) "Activa" else "Inactiva") },
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewPromotion,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva promoción")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionEditScreen(
    onSaved: () -> Unit,
    viewModel: PromotionEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    if (state.isLoading) {
        CenteredBox { CircularProgressIndicator() }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        var tipoExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(tipoExpanded, { tipoExpanded = it }) {
            OutlinedTextField(
                value = state.tipo.etiqueta,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(tipoExpanded, { tipoExpanded = false }) {
                PromotionType.entries.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.etiqueta) },
                        onClick = { viewModel.onTipoChange(tipo); tipoExpanded = false },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.valor,
            onValueChange = viewModel::onValorChange,
            label = {
                Text(
                    when (state.tipo) {
                        PromotionType.PORCENTAJE -> "Porcentaje (1-100)"
                        PromotionType.MONTO_FIJO -> "Monto a descontar"
                        PromotionType.PRECIO_ESPECIAL -> "Precio final"
                    }
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DiaField(
                label = "Inicio",
                dia = state.fechaInicio,
                onDia = viewModel::onFechaInicio,
                modifier = Modifier.weight(1f),
            )
            DiaField(
                label = "Fin",
                dia = state.fechaFin,
                onDia = viewModel::onFechaFin,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.fechaInicio.isNotBlank() && state.fechaFin.isNotBlank() &&
            state.fechaFin < state.fechaInicio
        ) {
            Text(
                "La fecha fin debe ser posterior al inicio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.notas,
            onValueChange = viewModel::onNotasChange,
            label = { Text("Notas") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (!state.isNew) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = state.activo, onCheckedChange = viewModel::onActivoChange)
                Text("  Activa")
            }
        }

        // ---------- Objetivos ----------
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Objetivos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::onOpenTargetDialog) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar objetivo")
            }
        }
        if (state.isNew) {
            state.draftTargets.forEach { target ->
                TargetRow(
                    nombre = state.targetNombre(target),
                    onRemove = { viewModel.onRemoveDraftTarget(target) },
                )
            }
            if (state.draftTargets.isEmpty()) {
                Text(
                    "Agrega al menos una categoría, producto o variante.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            state.savedTargets.forEach { display ->
                TargetRow(
                    nombre = display.nombre,
                    onRemove = { viewModel.onRemoveSavedTarget(display.target.id) },
                )
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
                    state.isNew -> "Crear promoción"
                    else -> "Guardar cambios"
                }
            )
        }
    }

    if (state.showTargetDialog) {
        TargetDialog(
            state = state,
            onDismiss = viewModel::onDismissTargetDialog,
            onAdd = viewModel::onAddTarget,
        )
    }
}

@Composable
private fun TargetRow(nombre: String, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(nombre, Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaField(
    label: String,
    dia: String,
    onDia: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = dia,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { showPicker = true },
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) { Text("Elegir") }
        },
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDia(millisADia(it)) }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private enum class TargetKind { CATEGORIA, PRODUCTO, VARIANTE }

@Composable
private fun TargetDialog(
    state: PromotionEditUiState,
    onDismiss: () -> Unit,
    onAdd: (TargetDraft) -> Unit,
) {
    var kind by remember { mutableStateOf(TargetKind.CATEGORIA) }
    var query by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar objetivo") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = kind == TargetKind.CATEGORIA,
                        onClick = { kind = TargetKind.CATEGORIA },
                        label = { Text("Categoría") },
                    )
                    FilterChip(
                        selected = kind == TargetKind.PRODUCTO,
                        onClick = { kind = TargetKind.PRODUCTO },
                        label = { Text("Producto") },
                    )
                    FilterChip(
                        selected = kind == TargetKind.VARIANTE,
                        onClick = { kind = TargetKind.VARIANTE },
                        label = { Text("Variante") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                val q = query.trim().lowercase()
                LazyColumn(Modifier.height(240.dp)) {
                    when (kind) {
                        TargetKind.CATEGORIA -> items(
                            state.categories.filter {
                                q.isBlank() || it.nombre.lowercase().contains(q)
                            },
                            key = { it.id },
                        ) { cat ->
                            Text(
                                cat.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(TargetDraft(categoryId = cat.id)) }
                                    .padding(vertical = 10.dp),
                            )
                            HorizontalDivider()
                        }

                        TargetKind.PRODUCTO -> items(
                            state.productNames.entries
                                .filter { q.isBlank() || it.value.lowercase().contains(q) }
                                .toList(),
                            key = { it.key },
                        ) { (id, nombre) ->
                            Text(
                                nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(TargetDraft(productId = id)) }
                                    .padding(vertical = 10.dp),
                            )
                            HorizontalDivider()
                        }

                        TargetKind.VARIANTE -> items(
                            state.variantIndex.values.filter {
                                q.isBlank() || it.sku.lowercase().contains(q) ||
                                    it.producto.lowercase().contains(q)
                            }.toList(),
                            key = { it.variantId },
                        ) { info ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(TargetDraft(variantId = info.variantId)) }
                                    .padding(vertical = 8.dp),
                            ) {
                                Text(info.producto)
                                Text(
                                    info.sku,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
