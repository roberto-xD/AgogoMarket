package com.passioagogo.market.ui.admin.events

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.passioagogo.market.core.images.ImageCompressor
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.events.Event
import com.passioagogo.market.data.events.EventDraft
import com.passioagogo.market.data.events.EventRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============ Lista ============

data class EventsUiState(
    val eventos: List<Event> = emptyList(),
    val widgetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val proximos: List<Event> get() = eventos.filter { EventDateTime.esFutura(it.vigenteHasta) }
    val pasados: List<Event> get() = eventos.filterNot { EventDateTime.esFutura(it.vigenteHasta) }
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    fun imageUrl(imagen: String) = repository.resolveImageUrl(imagen)

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val eventos = repository.getEvents()
            val widget = repository.isWidgetVisible()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    eventos = (eventos as? DataResult.Success)?.data ?: state.eventos,
                    widgetVisible = (widget as? DataResult.Success)?.data ?: state.widgetVisible,
                    errorMessage = (eventos as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    fun onToggleWidget(visible: Boolean) {
        _uiState.update { it.copy(widgetVisible = visible) }
        viewModelScope.launch {
            val r = repository.setWidgetVisible(visible)
            if (r is DataResult.Error) {
                // Revertir: el estado real es el del servidor
                _uiState.update {
                    it.copy(widgetVisible = !visible, errorMessage = r.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun EventsListScreen(
    onOpenEvent: (String) -> Unit,
    onNewEvent: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Widget de eventos", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (state.widgetVisible)
                                "Visible en la web con ${state.proximos.size} evento(s) próximo(s)"
                            else "Oculto en la web",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.widgetVisible,
                        onCheckedChange = viewModel::onToggleWidget,
                    )
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

            when {
                state.isLoading && state.eventos.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

                state.eventos.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    Alignment.Center,
                ) {
                    Text(
                        "Sin eventos registrados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn {
                    if (state.proximos.isNotEmpty()) {
                        item(key = "h-prox") { Encabezado("Próximos") }
                        items(state.proximos, key = { it.id }) { evento ->
                            EventoRow(evento, viewModel::imageUrl) { onOpenEvent(evento.id) }
                        }
                    }
                    if (state.pasados.isNotEmpty()) {
                        item(key = "h-pas") { Encabezado("Pasados") }
                        items(state.pasados, key = { it.id }) { evento ->
                            EventoRow(evento, viewModel::imageUrl) { onOpenEvent(evento.id) }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewEvent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nuevo evento")
        }
    }
}

@Composable
private fun Encabezado(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun EventoRow(
    evento: Event,
    imageUrl: (String) -> String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!evento.activo) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = "Inactivo",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(8.dp))
        }
        evento.imagen?.let { imagen ->
            AsyncImage(
                model = imageUrl(imagen),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.size(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(evento.titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                listOfNotNull(
                    EventDateTime.paraMostrar(evento.fechaInicio),
                    evento.lugar,
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider()
}

// ============ Editor ============

data class EventEditUiState(
    val eventId: String? = null,
    val titulo: String = "",
    val resumen: String = "",
    val detalles: String = "",
    val lugar: String = "",
    val enlace: String = "",
    val orden: String = "0",
    val activo: Boolean = true,
    val diaInicio: String = "",
    val horaInicio: String = "19:00",
    val tieneFin: Boolean = false,
    val diaFin: String = "",
    val horaFin: String = "21:00",
    val imagen: String? = null,
    val imagenPendiente: Uri? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
) {
    val isNew: Boolean get() = eventId == null

    val enlaceValido: Boolean
        get() = enlace.isBlank() || enlace.startsWith("http://") ||
            enlace.startsWith("https://") || enlace.startsWith("/")

    val isoInicio: String? get() = EventDateTime.aIsoUtc(diaInicio, horaInicio)
    val isoFin: String?
        get() = if (tieneFin) EventDateTime.aIsoUtc(diaFin, horaFin) else null

    /** El servidor exige fecha_fin >= fecha_inicio. */
    val rangoValido: Boolean
        get() = !tieneFin || (isoFin != null && isoInicio != null && isoFin!! >= isoInicio!!)

    val canSave: Boolean
        get() = !isSaving && titulo.isNotBlank() && diaInicio.isNotBlank() &&
            orden.toIntOrNull() != null && enlaceValido && rangoValido &&
            resumen.length <= 160
}

@HiltViewModel
class EventEditViewModel @Inject constructor(
    private val repository: EventRepository,
    private val imageCompressor: ImageCompressor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialId: String? = savedStateHandle["eventId"]

    private val _uiState = MutableStateFlow(EventEditUiState())
    val uiState: StateFlow<EventEditUiState> = _uiState.asStateFlow()

    init {
        if (initialId == null) {
            _uiState.update { it.copy(isLoading = false) }
        } else {
            viewModelScope.launch {
                val result = repository.getEvents()
                val evento = (result as? DataResult.Success)?.data?.firstOrNull { it.id == initialId }
                if (evento == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Evento no encontrado")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            eventId = evento.id,
                            titulo = evento.titulo,
                            resumen = evento.resumen.orEmpty(),
                            detalles = evento.detalles.orEmpty(),
                            lugar = evento.lugar.orEmpty(),
                            enlace = evento.enlace.orEmpty(),
                            orden = evento.orden.toString(),
                            activo = evento.activo,
                            imagen = evento.imagen,
                            diaInicio = EventDateTime.toDiaLocal(evento.fechaInicio),
                            horaInicio = EventDateTime.toHoraLocal(evento.fechaInicio),
                            tieneFin = evento.fechaFin != null,
                            diaFin = EventDateTime.toDiaLocal(evento.fechaFin),
                            horaFin = EventDateTime.toHoraLocal(evento.fechaFin)
                                .ifBlank { "21:00" },
                        )
                    }
                }
            }
        }
    }

    fun imageUrl(imagen: String) = repository.resolveImageUrl(imagen)

    fun onTitulo(v: String) = _uiState.update { it.copy(titulo = v) }
    fun onResumen(v: String) = _uiState.update { it.copy(resumen = v) }
    fun onDetalles(v: String) = _uiState.update { it.copy(detalles = v) }
    fun onLugar(v: String) = _uiState.update { it.copy(lugar = v) }
    fun onEnlace(v: String) = _uiState.update { it.copy(enlace = v) }
    fun onOrden(v: String) = _uiState.update { it.copy(orden = v) }
    fun onActivo(v: Boolean) = _uiState.update { it.copy(activo = v) }
    fun onDiaInicio(v: String) = _uiState.update { it.copy(diaInicio = v) }
    fun onHoraInicio(v: String) = _uiState.update { it.copy(horaInicio = v) }
    fun onDiaFin(v: String) = _uiState.update { it.copy(diaFin = v) }
    fun onHoraFin(v: String) = _uiState.update { it.copy(horaFin = v) }
    fun onImagePicked(uri: Uri?) = _uiState.update { it.copy(imagenPendiente = uri) }
    fun onAskDelete() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun onDismissDelete() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun onTieneFin(v: Boolean) = _uiState.update {
        it.copy(tieneFin = v, diaFin = if (v && it.diaFin.isBlank()) it.diaInicio else it.diaFin)
    }

    fun onDelete() {
        val id = _uiState.value.eventId ?: return
        _uiState.update { it.copy(isSaving = true, showDeleteConfirm = false) }
        viewModelScope.launch {
            when (val r = repository.deleteEvent(id)) {
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
        val inicio = state.isoInicio ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            var ruta = state.imagen
            state.imagenPendiente?.let { uri ->
                val bytes = imageCompressor.compress(uri).getOrElse { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "No se pudo procesar la imagen: " +
                                (e.message ?: "error desconocido"),
                        )
                    }
                    return@launch
                }
                when (val subida = repository.uploadImage(bytes)) {
                    is DataResult.Success -> ruta = subida.data
                    is DataResult.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = subida.error.toMessage())
                        }
                        return@launch
                    }
                }
            }

            val result = if (state.isNew) {
                repository.createEvent(
                    EventDraft(
                        titulo = state.titulo.trim(),
                        resumen = state.resumen.trim().ifBlank { null },
                        detalles = state.detalles.ifBlank { null },
                        lugar = state.lugar.trim().ifBlank { null },
                        imagen = ruta,
                        fechaInicio = inicio,
                        fechaFin = state.isoFin,
                        enlace = state.enlace.trim().ifBlank { null },
                        orden = state.orden.toInt(),
                    )
                )
            } else {
                repository.updateEvent(
                    Event(
                        id = state.eventId!!,
                        titulo = state.titulo.trim(),
                        resumen = state.resumen.trim().ifBlank { null },
                        detalles = state.detalles.ifBlank { null },
                        lugar = state.lugar.trim().ifBlank { null },
                        imagen = ruta,
                        fechaInicio = inicio,
                        fechaFin = state.isoFin,
                        enlace = state.enlace.trim().ifBlank { null },
                        orden = state.orden.toInt(),
                        activo = state.activo,
                        vigenteHasta = null,
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

@Composable
fun EventEditScreen(
    onSaved: () -> Unit,
    viewModel: EventEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onImagePicked(uri) }

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
        val modelo = state.imagenPendiente ?: state.imagen?.let { viewModel.imageUrl(it) }
        if (modelo != null) {
            AsyncImage(
                model = modelo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.height(8.dp))
        }
        OutlinedButton(
            onClick = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (modelo == null) "Elegir imagen (opcional)" else "Cambiar imagen")
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.titulo,
            onValueChange = viewModel::onTitulo,
            label = { Text("Título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.resumen,
            onValueChange = viewModel::onResumen,
            label = { Text("Resumen (lo que cabe en el widget)") },
            supportingText = { Text("${state.resumen.length}/160") },
            isError = state.resumen.length > 160,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.lugar,
            onValueChange = viewModel::onLugar,
            label = { Text("Lugar") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // ---------- Fechas ----------
        Spacer(Modifier.height(16.dp))
        Text("Inicio", style = MaterialTheme.typography.labelLarge)
        FechaHoraFila(
            dia = state.diaInicio,
            hora = state.horaInicio,
            onDia = viewModel::onDiaInicio,
            onHora = viewModel::onHoraInicio,
        )

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.tieneFin, onCheckedChange = viewModel::onTieneFin)
            Text("  Tiene fecha de fin")
        }
        if (state.tieneFin) {
            FechaHoraFila(
                dia = state.diaFin,
                hora = state.horaFin,
                onDia = viewModel::onDiaFin,
                onHora = viewModel::onHoraFin,
            )
            if (!state.rangoValido) {
                Text(
                    "El fin debe ser posterior al inicio",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.detalles,
            onValueChange = viewModel::onDetalles,
            label = { Text("Detalles") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.enlace,
            onValueChange = viewModel::onEnlace,
            label = { Text("Enlace (boletos, registro…)") },
            placeholder = { Text("https://… o /eventos") },
            singleLine = true,
            isError = !state.enlaceValido,
            supportingText = {
                if (!state.enlaceValido) {
                    Text("Debe empezar con https:// o con /")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
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
                Text("  Visible en la web")
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
                    state.isNew -> "Crear evento"
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
                Text("  Eliminar evento")
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDelete,
            title = { Text("Eliminar evento") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FechaHoraFila(
    dia: String,
    hora: String,
    onDia: (String) -> Unit,
    onHora: (String) -> Unit,
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = dia,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            trailingIcon = { TextButton(onClick = { showDate = true }) { Text("Elegir") } },
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = hora,
            onValueChange = {},
            readOnly = true,
            label = { Text("Hora") },
            trailingIcon = { TextButton(onClick = { showTime = true }) { Text("Elegir") } },
            modifier = Modifier.weight(1f),
        )
    }

    if (showDate) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDia(EventDateTime.diaDesdeMillis(it)) }
                    showDate = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text("Cancelar") }
            },
        ) { DatePicker(state = pickerState) }
    }

    if (showTime) {
        val partes = hora.split(":").mapNotNull { it.toIntOrNull() }
        val timeState = rememberTimePickerState(
            initialHour = partes.getOrElse(0) { 19 },
            initialMinute = partes.getOrElse(1) { 0 },
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Hora") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onHora("%02d:%02d".format(timeState.hour, timeState.minute))
                    showTime = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Cancelar") }
            },
        )
    }
}
