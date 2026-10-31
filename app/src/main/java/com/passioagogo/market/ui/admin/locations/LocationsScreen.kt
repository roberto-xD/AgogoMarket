package com.passioagogo.market.ui.admin.locations

import com.passioagogo.market.ui.common.OpcionesTeclado
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.LocationType
import com.passioagogo.market.domain.inventory.DireccionEnvioDraft
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
    /** Ubicación cuya dirección de envío se está capturando. */
    val editandoEnvio: Location? = null,
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
    fun onEditarEnvio(location: Location) =
        _uiState.update { it.copy(editandoEnvio = location) }

    fun onCerrarEnvio() = _uiState.update { it.copy(editandoEnvio = null) }

    fun onGuardarEnvio(draft: DireccionEnvioDraft) {
        val location = _uiState.value.editandoEnvio ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = locationRepository.updateDireccionEnvio(location.id, draft)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, editandoEnvio = null) }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onMarcarOrigen(location: Location) {
        viewModelScope.launch {
            locationRepository.marcarComoOrigen(location.id)
            refresh()
        }
    }

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
fun LocationsScreen(
    onBack: ()-> Unit,
    viewModel: LocationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        onBack()
    }
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    location.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (location.esOrigen) {
                                    Spacer(Modifier.width(8.dp))
                                    AssistChip(
                                        onClick = { viewModel.onEditarEnvio(location) },
                                        label = { Text("Origen de envíos") },
                                    )
                                }
                            }
                            Text(
                                listOfNotNull(
                                    location.tipo.etiqueta,
                                    location.direccion,
                                ).joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        // Solo importa en el origen: es de donde sale la guía
                        if (location.esOrigen && !location.listaParaEnviar) {
                            Text(
                                "Falta: " + location.camposFaltantes.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        IconButton(onClick = { viewModel.onEditarEnvio(location) }) {
                            Icon(
                                Icons.Filled.LocalShipping,
                                contentDescription = "Dirección de envío",
                                tint = if (location.esOrigen && !location.listaParaEnviar) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
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

    state.editandoEnvio?.let { location ->
        DireccionEnvioDialog(
            location = location,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = viewModel::onCerrarEnvio,
            onGuardar = viewModel::onGuardarEnvio,
            onMarcarOrigen = { viewModel.onMarcarOrigen(location) },
        )
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
                    keyboardOptions = OpcionesTeclado.Texto,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    keyboardOptions = OpcionesTeclado.Texto,
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


/**
 * Dirección de envío de una ubicación, desglosada como la pide la
 * paquetería. El texto libre `direccion` se conserva aparte: esto no lo
 * sustituye, lo complementa.
 */
@Composable
private fun DireccionEnvioDialog(
    location: Location,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onGuardar: (DireccionEnvioDraft) -> Unit,
    onMarcarOrigen: () -> Unit,
) {
    var calle by remember { mutableStateOf(location.calle.orEmpty()) }
    var numero by remember { mutableStateOf(location.numero.orEmpty()) }
    var interior by remember { mutableStateOf(location.interior.orEmpty()) }
    var colonia by remember { mutableStateOf(location.colonia.orEmpty()) }
    var ciudad by remember { mutableStateOf(location.ciudad.orEmpty()) }
    var estado by remember { mutableStateOf(location.estado.orEmpty()) }
    var estadoCodigo by remember { mutableStateOf(location.estadoCodigo.orEmpty()) }
    var cp by remember { mutableStateOf(location.cp.orEmpty()) }
    var telefono by remember { mutableStateOf(location.telefono.orEmpty()) }
    var contacto by remember { mutableStateOf(location.contacto.orEmpty()) }
    var referencias by remember { mutableStateOf(location.referencias.orEmpty()) }

    // Mismos formatos que valida la base: se ven al capturar, no al
    // generar la guía.
    val cpValido = cp.isBlank() || cp.matches(Regex("^[0-9]{5}$"))
    val codigoValido = estadoCodigo.isBlank() ||
        estadoCodigo.uppercase().matches(Regex("^[A-Z]{2}$"))
    val valido = cpValido && codigoValido

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dirección de envío") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    location.nombre,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (!location.esOrigen) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onMarcarOrigen) {
                        Text("Usar como origen de los envíos")
                    }
                    Text(
                        "Solo una ubicación puede serlo: marcar esta apaga la actual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calle,
                        onValueChange = { calle = it },
                        label = { Text("Calle") },
                        singleLine = true,
                        keyboardOptions = OpcionesTeclado.Texto,
                        modifier = Modifier.weight(2f),
                    )
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = { Text("Número") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = interior,
                    onValueChange = { interior = it },
                    label = { Text("Interior (opcional)") },
                    singleLine = true,
                    keyboardOptions = OpcionesTeclado.Texto,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = colonia,
                    onValueChange = { colonia = it },
                    label = { Text("Colonia") },
                    singleLine = true,
                    keyboardOptions = OpcionesTeclado.Texto,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cp,
                        onValueChange = { if (it.length <= 5) cp = it },
                        label = { Text("CP") },
                        singleLine = true,
                        isError = !cpValido,
                        supportingText = { Text(if (!cpValido) "5 dígitos" else "") },
                        keyboardOptions = OpcionesTeclado.Numero,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = ciudad,
                        onValueChange = { ciudad = it },
                        label = { Text("Ciudad") },
                        singleLine = true,
                        keyboardOptions = OpcionesTeclado.Texto,
                        modifier = Modifier.weight(2f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = { estado = it },
                        label = { Text("Estado") },
                        singleLine = true,
                        keyboardOptions = OpcionesTeclado.Texto,
                        modifier = Modifier.weight(2f),
                    )
                    OutlinedTextField(
                        value = estadoCodigo,
                        onValueChange = { if (it.length <= 2) estadoCodigo = it.uppercase() },
                        label = { Text("Código") },
                        singleLine = true,
                        isError = !codigoValido,
                        supportingText = { Text("CDMX = CX") },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Contacto para la recolección",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    "La paquetería lo exige: es por donde llama el repartidor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Nombre de quien entrega") },
                    singleLine = true,
                    keyboardOptions = OpcionesTeclado.Texto,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    keyboardOptions = OpcionesTeclado.Telefono,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = referencias,
                    onValueChange = { referencias = it },
                    label = { Text("Referencias") },
                    keyboardOptions = OpcionesTeclado.Texto,
                    modifier = Modifier.fillMaxWidth(),
                )

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !isSaving,
                onClick = {
                    onGuardar(
                        DireccionEnvioDraft(
                            calle = calle, numero = numero, interior = interior,
                            colonia = colonia, ciudad = ciudad, estado = estado,
                            estadoCodigo = estadoCodigo, cp = cp,
                            telefono = telefono, contacto = contacto,
                            referencias = referencias,
                        )
                    )
                },
            ) { Text(if (isSaving) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
