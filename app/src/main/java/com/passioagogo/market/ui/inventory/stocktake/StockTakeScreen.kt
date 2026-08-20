package com.passioagogo.market.ui.inventory.stocktake

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
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
import com.passioagogo.market.ui.pos.scanner.BarcodeScannerView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Línea de conteo: existencia actual en el sistema vs. la contada. */
data class ConteoLinea(
    val info: VariantInfo,
    val actual: Int,
    val contado: String,
) {
    val contadoValido: Boolean get() = contado.toIntOrNull()?.let { it >= 0 } == true
    val diferencia: Int? get() = contado.toIntOrNull()?.minus(actual)
}

data class StockTakeUiState(
    val locations: List<Location> = emptyList(),
    val locationId: String? = null,
    val query: String = "",
    val catalog: List<VariantInfo> = emptyList(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    /** Existencias actuales de la ubicación, por variante. */
    val stockActual: Map<String, Int> = emptyMap(),
    val lineas: Map<String, ConteoLinea> = emptyMap(),
    /** Última variante agregada: recibe el foco al aparecer en la lista. */
    val recienAgregada: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showScanner: Boolean = false,
    val scanMessage: String? = null,
    val errorMessage: String? = null,
    val showConfirm: Boolean = false,
    val resultado: String? = null,
) {
    val searchResults: List<VariantInfo>
        get() {
            if (query.isBlank()) return emptyList()
            val q = query.trim().lowercase()
            return catalog.filter {
                it.producto.lowercase().contains(q) || it.sku.lowercase().contains(q)
            }.take(15)
        }

    val canSave: Boolean
        get() = !isSaving && locationId != null && lineas.isNotEmpty() &&
            lineas.values.all { it.contadoValido }
}

@HiltViewModel
class StockTakeViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockTakeUiState())
    val uiState: StateFlow<StockTakeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val locs = locationRepository.getLocations()
            val index = catalogRepository.variantIndex()
            val guardada = userPreferences.activeLocationId.first()
            val lista = (locs as? DataResult.Success)?.data ?: emptyList()
            val inicial = guardada?.takeIf { id -> lista.any { it.id == id } }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    locations = lista,
                    locationId = inicial,
                    catalog = index.values.sortedBy { v -> v.producto },
                    variantIndex = index,
                )
            }
            inicial?.let { cargarStock(it) }
        }
    }

    fun onLocationSelected(id: String) {
        // Cambiar de ubicación descarta el conteo: las cantidades no son
        // trasladables de una tienda a otra.
        _uiState.update {
            it.copy(locationId = id, lineas = emptyMap(), stockActual = emptyMap())
        }
        viewModelScope.launch { cargarStock(id) }
    }

    private suspend fun cargarStock(locationId: String) {
        when (val result = inventoryRepository.getStock(locationId = locationId)) {
            is DataResult.Success -> _uiState.update { state ->
                state.copy(stockActual = result.data.associate { it.variantId to it.cantidad })
            }
            is DataResult.Error -> _uiState.update {
                it.copy(errorMessage = result.error.toMessage())
            }
        }
    }

    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }
    fun onOpenScanner() = _uiState.update { it.copy(showScanner = true, scanMessage = null) }
    fun onCloseScanner() = _uiState.update { it.copy(showScanner = false, scanMessage = null) }
    fun onAskConfirm() = _uiState.update { it.copy(showConfirm = true) }
    fun onDismissConfirm() = _uiState.update { it.copy(showConfirm = false) }
    fun onDismissResultado() = _uiState.update { it.copy(resultado = null) }

    fun onAddVariant(info: VariantInfo) = _uiState.update { state ->
        if (state.lineas.containsKey(info.variantId)) {
            // Ya estaba: no se duplica, pero igual se enfoca para corregirla
            state.copy(query = "", recienAgregada = info.variantId)
        } else {
            val actual = state.stockActual[info.variantId] ?: 0
            state.copy(
                query = "",
                recienAgregada = info.variantId,
                lineas = state.lineas + (
                    info.variantId to ConteoLinea(
                        info = info,
                        actual = actual,
                        contado = actual.toString(),
                    )
                    ),
            )
        }
    }

    /** El foco se consume una sola vez: si no, volvería al mismo campo. */
    fun onFocoConsumido() = _uiState.update { it.copy(recienAgregada = null) }

    fun onContadoChange(variantId: String, valor: String) = _uiState.update { state ->
        val linea = state.lineas[variantId] ?: return@update state
        state.copy(lineas = state.lineas + (variantId to linea.copy(contado = valor)))
    }

    fun onRemove(variantId: String) =
        _uiState.update { it.copy(lineas = it.lineas - variantId) }

    /** Precarga todas las variantes que ya tienen existencia en la ubicación. */
    fun onCargarExistentes() = _uiState.update { state ->
        val nuevas = state.stockActual.mapNotNull { (variantId, cantidad) ->
            val info = state.variantIndex[variantId] ?: return@mapNotNull null
            variantId to ConteoLinea(info = info, actual = cantidad, contado = cantidad.toString())
        }.toMap()
        state.copy(lineas = state.lineas + nuevas)
    }

    private val cooldown = mutableMapOf<String, Long>()

    fun onBarcodeScanned(code: String) {
        val now = System.currentTimeMillis()
        if (now - (cooldown[code] ?: 0L) < 2_000) return
        cooldown[code] = now

        viewModelScope.launch {
            val variant = catalogRepository.findVariantBySku(code)
            val info = variant?.let { _uiState.value.variantIndex[it.id] }
            if (info == null) {
                _uiState.update { it.copy(scanMessage = "SKU no encontrado: $code") }
            } else {
                onAddVariant(info)
                _uiState.update { it.copy(scanMessage = "Agregado: ${info.producto}") }
            }
        }
    }

    /**
     * Fija la existencia de cada línea al valor contado. Es una escritura
     * absoluta y sin rastro documental: por eso solo el admin puede hacerla
     * y se pide confirmación explícita.
     */
    fun onAplicar() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, showConfirm = false, errorMessage = null) }
        viewModelScope.launch {
            var aplicadas = 0
            val fallos = mutableListOf<String>()
            state.lineas.forEach { (variantId, linea) ->
                val cantidad = linea.contado.toIntOrNull() ?: return@forEach
                when (
                    val r = inventoryRepository.setStock(
                        variantId = variantId,
                        locationId = state.locationId!!,
                        cantidad = cantidad,
                    )
                ) {
                    is DataResult.Success -> aplicadas++
                    is DataResult.Error -> fallos += "${linea.info.sku}: ${r.error.toMessage()}"
                }
            }
            cargarStock(state.locationId!!)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    lineas = if (fallos.isEmpty()) emptyMap() else it.lineas,
                    resultado = buildString {
                        append("$aplicadas existencia(s) actualizada(s).")
                        if (fallos.isNotEmpty()) {
                            append("\n\nNo se aplicaron:\n")
                            append(fallos.joinToString("\n"))
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTakeScreen(viewModel: StockTakeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val teclado = LocalSoftwareKeyboardController.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.onOpenScanner() }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ---------- Escáner a pantalla completa ----------
    if (state.showScanner) {
        Dialog(
            onDismissRequest = viewModel::onCloseScanner,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(Modifier.fillMaxSize()) {
                BarcodeScannerView(
                    onBarcode = viewModel::onBarcodeScanned,
                    modifier = Modifier.fillMaxSize(),
                )
                state.scanMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                Button(
                    onClick = viewModel::onCloseScanner,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                ) { Text("Listo (${state.lineas.size} productos)") }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            // Sin imePadding: el manifest declara adjustResize, que ya encoge
            // la ventana al abrir el teclado. Aplicar ambos resta la altura
            // del teclado dos veces y deja un hueco muerto bajo el contenido.
            .padding(16.dp)
    ) {
        Text(
            "Fija la existencia real de cada producto. Sustituye la cantidad " +
                "actual, no la suma.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded, { expanded = it }) {
            OutlinedTextField(
                value = state.locations.firstOrNull { it.id == state.locationId }?.nombre ?: "",
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar producto o SKU") },
                singleLine = true,
                enabled = state.locationId != null,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                enabled = state.locationId != null,
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) viewModel.onOpenScanner()
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear")
            }
        }

        if (state.stockActual.isNotEmpty() && state.query.isBlank()) {
            TextButton(onClick = viewModel::onCargarExistentes) {
                Text("Cargar los ${state.stockActual.size} productos con existencia")
            }
        }

        val listState = rememberLazyListState()
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(state.searchResults, key = { "s-${it.variantId}" }) { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAddVariant(info) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(info.producto)
                        Text(
                            info.sku,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.Add, contentDescription = "Agregar")
                }
                HorizontalDivider()
            }

            if (state.query.isBlank()) {
                val entradas = state.lineas.entries.toList()
                itemsIndexed(entradas, key = { _, e -> "l-${e.key}" }) { indice, entrada ->
                    val id = entrada.key
                    val linea = entrada.value
                    val focusRequester = remember { FocusRequester() }

                    // Al agregar, desplazar y enfocar la línea nueva.
                    //
                    // Mientras el escáner está abierto NO se pide foco: el campo
                    // vive detrás del diálogo y al enfocarlo se lo robaría,
                    // cerrando la cámara tras cada lectura. El foco se aplica
                    // cuando el escáner se cierra (la clave incluye showScanner
                    // para que el efecto vuelva a evaluarse en ese momento).
                    LaunchedEffect(state.recienAgregada, state.showScanner) {
                        if (state.recienAgregada == id && !state.showScanner) {
                            listState.animateScrollToItem(indice)
                            focusRequester.requestFocus()
                            viewModel.onFocoConsumido()
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(linea.info.producto)
                            Text(
                                buildString {
                                    append(linea.info.sku)
                                    append("  ·  actual: ${linea.actual}")
                                    linea.diferencia?.let { d ->
                                        if (d != 0) append("  ·  ${if (d > 0) "+$d" else "$d"}")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if ((linea.diferencia ?: 0) != 0)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = linea.contado,
                            onValueChange = { viewModel.onContadoChange(id, it) },
                            label = { Text("Real") },
                            singleLine = true,
                            isError = !linea.contadoValido,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { teclado?.hide() },
                            ),
                            modifier = Modifier
                                .width(96.dp)
                                .focusRequester(focusRequester),
                        )
                        IconButton(onClick = { viewModel.onRemove(id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
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
            )
        }

        Button(
            onClick = viewModel::onAskConfirm,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.isSaving) "Aplicando…"
                else "Aplicar a ${state.lineas.size} producto(s)"
            )
        }
    }

    if (state.showConfirm) {
        val conCambio = state.lineas.values.count { (it.diferencia ?: 0) != 0 }
        AlertDialog(
            onDismissRequest = viewModel::onDismissConfirm,
            title = { Text("Aplicar existencias") },
            text = {
                Text(
                    "Se fijará la existencia de ${state.lineas.size} producto(s) " +
                        "($conCambio con cambio) en la ubicación seleccionada.\n\n" +
                        "Este ajuste no genera compra ni venta: es una corrección " +
                        "directa del inventario."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onAplicar) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissConfirm) { Text("Cancelar") }
            },
        )
    }

    state.resultado?.let { resultado ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissResultado,
            title = { Text("Resultado") },
            text = { Text(resultado, fontWeight = FontWeight.Normal) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissResultado) { Text("Entendido") }
            },
        )
    }
}
