package com.passioagogo.market.ui.admin.gallery

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.passioagogo.market.data.gallery.GalleryDraft
import com.passioagogo.market.data.gallery.GalleryItem
import com.passioagogo.market.data.gallery.GalleryRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============ Lista ============

data class GalleryListUiState(
    val items: List<GalleryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class GalleryListViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryListUiState())
    val uiState: StateFlow<GalleryListUiState> = _uiState.asStateFlow()

    fun imageUrl(imagen: String) = galleryRepository.resolveImageUrl(imagen)

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = galleryRepository.getItems()
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(isLoading = false, items = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun GalleryListScreen(
    onOpenItem: (String) -> Unit,
    onNewItem: () -> Unit,
    viewModel: GalleryListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.items.isEmpty() ->
                Centered { CircularProgressIndicator() }

            state.errorMessage != null -> Centered {
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

            state.items.isEmpty() -> Centered {
                Text(
                    "La galería está vacía",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyColumn {
                items(state.items, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenItem(item.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!item.activo) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Inactivo",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 2.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                        AsyncImage(
                            model = viewModel.imageUrl(item.imagen),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.titulo, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOfNotNull(
                                    "Orden ${item.orden}",
                                    item.categoria,
                                ).joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        FloatingActionButton(
            onClick = onNewItem,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nuevo elemento")
        }
    }
}

// ============ Editor ============

data class GalleryEditUiState(
    val itemId: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val detalles: String = "",
    val categoria: String = "",
    val orden: String = "0",
    val activo: Boolean = true,
    /** Ruta ya guardada en la tabla (relativa o URL). */
    val imagen: String? = null,
    /** Imagen elegida y aún no subida. */
    val imagenPendiente: Uri? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirm: Boolean = false,
    val saved: Boolean = false,
) {
    val isNew: Boolean get() = itemId == null

    val canSave: Boolean
        get() = !isSaving && titulo.isNotBlank() &&
            orden.toIntOrNull() != null &&
            (imagen != null || imagenPendiente != null)
}

@HiltViewModel
class GalleryEditViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val imageCompressor: ImageCompressor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialId: String? = savedStateHandle["itemId"]

    private val _uiState = MutableStateFlow(GalleryEditUiState())
    val uiState: StateFlow<GalleryEditUiState> = _uiState.asStateFlow()

    init {
        if (initialId == null) {
            _uiState.update { it.copy(isLoading = false) }
        } else {
            viewModelScope.launch {
                val result = galleryRepository.getItems()
                val item = (result as? DataResult.Success)?.data?.firstOrNull { it.id == initialId }
                if (item == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Elemento no encontrado")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            itemId = item.id,
                            titulo = item.titulo,
                            descripcion = item.descripcion.orEmpty(),
                            detalles = item.detalles.orEmpty(),
                            categoria = item.categoria.orEmpty(),
                            orden = item.orden.toString(),
                            activo = item.activo,
                            imagen = item.imagen,
                        )
                    }
                }
            }
        }
    }

    fun imageUrl(imagen: String) = galleryRepository.resolveImageUrl(imagen)

    fun onTitulo(v: String) = _uiState.update { it.copy(titulo = v) }
    fun onDescripcion(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun onDetalles(v: String) = _uiState.update { it.copy(detalles = v) }
    fun onCategoria(v: String) = _uiState.update { it.copy(categoria = v) }
    fun onOrden(v: String) = _uiState.update { it.copy(orden = v) }
    fun onActivo(v: Boolean) = _uiState.update { it.copy(activo = v) }
    fun onImagePicked(uri: Uri?) = _uiState.update { it.copy(imagenPendiente = uri) }
    fun onAskDelete() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun onDismissDelete() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun onDelete() {
        val id = _uiState.value.itemId ?: return
        _uiState.update { it.copy(isSaving = true, showDeleteConfirm = false) }
        viewModelScope.launch {
            when (val r = galleryRepository.deleteItem(id)) {
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
            // 1) Subir la imagen nueva, si la hay (misma compresión del catálogo)
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
                when (val subida = galleryRepository.uploadImage(bytes)) {
                    is DataResult.Success -> ruta = subida.data
                    is DataResult.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = subida.error.toMessage())
                        }
                        return@launch
                    }
                }
            }
            val imagenFinal = ruta ?: return@launch

            // 2) Guardar el registro
            val result = if (state.isNew) {
                galleryRepository.createItem(
                    GalleryDraft(
                        titulo = state.titulo.trim(),
                        descripcion = state.descripcion.ifBlank { null },
                        detalles = state.detalles.ifBlank { null },
                        imagen = imagenFinal,
                        categoria = state.categoria.ifBlank { null },
                        orden = state.orden.toInt(),
                    )
                )
            } else {
                galleryRepository.updateItem(
                    GalleryItem(
                        id = state.itemId!!,
                        titulo = state.titulo.trim(),
                        descripcion = state.descripcion.ifBlank { null },
                        detalles = state.detalles.ifBlank { null },
                        imagen = imagenFinal,
                        categoria = state.categoria.ifBlank { null },
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

@Composable
fun GalleryEditScreen(
    onSaved: () -> Unit,
    viewModel: GalleryEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onImagePicked(uri) }

    if (state.isLoading) {
        Centered { CircularProgressIndicator() }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ---------- Imagen ----------
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
            Text(if (modelo == null) "Elegir imagen" else "Cambiar imagen")
        }
        if (state.imagenPendiente != null) {
            Text(
                "La imagen se subirá al guardar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            value = state.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = { Text("Descripción (resumen corto)") },
            supportingText = { Text("${state.descripcion.length}/300") },
            isError = state.descripcion.length > 300,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.detalles,
            onValueChange = viewModel::onDetalles,
            label = { Text("Detalles (texto largo del diálogo)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.categoria,
                onValueChange = viewModel::onCategoria,
                label = { Text("Categoría") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.orden,
                onValueChange = viewModel::onOrden,
                label = { Text("Orden") },
                singleLine = true,
                isError = state.orden.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.size(width = 110.dp, height = 60.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.activo, onCheckedChange = viewModel::onActivo)
            Text("  Visible en la web")
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
                    state.isNew -> "Agregar a la galería"
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
                Text("  Eliminar de la galería")
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDelete,
            title = { Text("Eliminar elemento") },
            text = {
                Text(
                    "Se quitará del carrusel de la web. La imagen permanecerá " +
                        "en el almacenamiento."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onDelete) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDelete) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
