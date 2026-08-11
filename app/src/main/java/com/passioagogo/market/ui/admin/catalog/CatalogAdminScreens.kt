package com.passioagogo.market.ui.admin.catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.passioagogo.market.ui.pos.scanner.BarcodeScannerView
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.catalog.Category

// ============ Catálogo: productos + categorías ============

@Composable
fun CatalogAdminScreen(
    onOpenProduct: (String) -> Unit,
    onNewProduct: () -> Unit,
    viewModel: CatalogAdminViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selectedTab == 0, { selectedTab = 0 }, text = { Text("Productos") })
                Tab(selectedTab == 1, { selectedTab = 1 }, text = { Text("Categorías") })
            }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        label = { Text("Buscar") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                FilterChip(
                    selected = state.showInactive,
                    onClick = { viewModel.onToggleInactive(!state.showInactive) },
                    label = { Text("Ver inactivos") },
                )
                IconButton(onClick = viewModel::refresh, enabled = !state.isRefreshing) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
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

            when (selectedTab) {
                0 -> LazyColumn {
                    items(state.filteredProducts, key = { it.product.id }) { pw ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenProduct(pw.product.id) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!pw.product.activo) InactiveIcon()
                            pw.product.imagenes.firstOrNull()?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                )
                                Spacer(Modifier.height(0.dp).padding(horizontal = 4.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(pw.product.nombre, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${pw.variants.size} variante(s)" +
                                        (pw.product.marca?.let { " · $it" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }

                1 -> LazyColumn {
                    items(state.visibleCategories, key = { it.id }) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onEditCategory(category) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!category.activo) InactiveIcon()
                            Column(Modifier.weight(1f)) {
                                Text(category.nombre, style = MaterialTheme.typography.bodyLarge)
                                val parent = state.categories
                                    .firstOrNull { it.id == category.parentId }?.nombre
                                if (parent != null) {
                                    Text(
                                        "Subcategoría de $parent",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { if (selectedTab == 0) onNewProduct() else viewModel.onNewCategory() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nuevo")
        }
    }

    if (state.showCategoryDialog) {
        CategoryDialog(
            categories = state.categories,
            editing = state.editingCategory,
            isSaving = state.isSavingCategory,
            onDismiss = viewModel::onDismissCategoryDialog,
            onSave = viewModel::onSaveCategory,
        )
    }
}

/** Marca visual de registro inactivo, al inicio de la fila. */
@Composable
private fun InactiveIcon() {
    Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = "Inactivo",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .size(20.dp)
            .padding(end = 2.dp),
    )
    Spacer(Modifier.width(8.dp))
}

@Composable
private fun InactiveBadge() {
    Text(
        "Inactivo",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    categories: List<Category>,
    editing: Category?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (nombre: String, descripcion: String?, parentId: String?, activo: Boolean) -> Unit,
) {
    var nombre by remember { mutableStateOf(editing?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(editing?.descripcion ?: "") }
    var parentId by remember { mutableStateOf(editing?.parentId) }
    var activo by remember { mutableStateOf(editing?.activo ?: true) }
    var expanded by remember { mutableStateOf(false) }

    val parentOptions = categories.filter { it.activo && it.id != editing?.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Nueva categoría" else "Editar categoría") },
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
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = parentOptions.firstOrNull { it.id == parentId }?.nombre
                            ?: "Sin categoría padre",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría padre") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Sin categoría padre") },
                            onClick = { parentId = null; expanded = false },
                        )
                        parentOptions.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = { parentId = cat.id; expanded = false },
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
                onClick = { onSave(nombre.trim(), descripcion.ifBlank { null }, parentId, activo) },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

// ============ Edición de producto ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    viewModel: ProductEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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
        OutlinedTextField(
            value = state.marca,
            onValueChange = viewModel::onMarcaChange,
            label = { Text("Marca") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded, { expanded = it }) {
            OutlinedTextField(
                value = state.categories.firstOrNull { it.id == state.categoryId }?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                supportingText = if (state.categories.isEmpty()) {
                    { Text("No hay categorías activas: crea una primero") }
                } else null,
                isError = state.categories.isEmpty(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                state.categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.nombre) },
                        onClick = {
                            viewModel.onCategorySelected(cat.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.attributesText,
            onValueChange = viewModel::onAttributesChange,
            label = { Text("Atributos (una línea: clave: valor)") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (!state.isNew) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = state.activo, onCheckedChange = viewModel::onActivoChange)
                Spacer(Modifier.height(0.dp))
                Text("  Activo")
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

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.onSaveProduct()
            },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                when {
                    state.isSaving -> "Guardando…"
                    state.isNew -> "Crear producto"
                    else -> "Guardar cambios"
                }
            )
        }

        // ---------- Imágenes ----------
        if (!state.isNew) {
            Spacer(Modifier.height(24.dp))
            val picker = rememberLauncherForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
            ) { uris -> viewModel.onImagesPicked(uris) }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Imágenes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    enabled = !state.isUploadingImages,
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                ) {
                    Text(if (state.isUploadingImages) "Subiendo…" else "Agregar (máx. 5)")
                }
            }
            if (state.imagenes.isEmpty()) {
                Text(
                    "Sin imágenes. La primera será la portada del producto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.imagenes, key = { it }) { url ->
                        ProductImageThumb(
                            url = url,
                            esPrincipal = url == state.imagenes.first(),
                            onSetPrincipal = { viewModel.onSetPrincipal(url) },
                            onRemove = { viewModel.onRemoveImage(url) },
                        )
                    }
                }
            }
        }

        // ---------- Variantes ----------
        if (!state.isNew) {
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Variantes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = viewModel::onNewVariant) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva variante")
                }
            }
            state.variants.forEach { variant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onEditVariant(variant) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(variant.sku, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Precio ${variant.precioVenta} · Costo ${variant.costo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!variant.activo) InactiveBadge()
                }
                HorizontalDivider()
            }
            if (state.variants.isEmpty()) {
                Text(
                    "Sin variantes: agrega al menos una para poder vender este producto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Spacer(Modifier.height(16.dp))
            Text(
                "Guarda el producto para poder agregar variantes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    state.variantDialog?.let { dialog ->
        VariantDialog(
            dialog = dialog,
            isSaving = state.isSaving,
            onChange = viewModel::onVariantDialogChange,
            onDismiss = viewModel::onDismissVariantDialog,
            onSave = viewModel::onSaveVariant,
        )
    }
}

@Composable
private fun VariantDialog(
    dialog: VariantDialogState,
    isSaving: Boolean,
    onChange: (VariantDialogState) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    var scanning by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) scanning = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dialog.editing == null) "Nueva variante" else "Editar variante") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = dialog.sku,
                    onValueChange = { onChange(dialog.copy(sku = it)) },
                    label = { Text("SKU") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) scanning = true
                                else permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                        ) {
                            Icon(
                                Icons.Filled.QrCodeScanner,
                                contentDescription = "Escanear código de barras",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dialog.precio,
                    onValueChange = { onChange(dialog.copy(precio = it)) },
                    label = { Text("Precio de venta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dialog.costo,
                    onValueChange = { onChange(dialog.copy(costo = it)) },
                    label = { Text("Costo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dialog.attributesText,
                    onValueChange = { onChange(dialog.copy(attributesText = it)) },
                    label = { Text("Atributos (clave: valor)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (dialog.editing != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = dialog.activo,
                            onCheckedChange = { onChange(dialog.copy(activo = it)) },
                        )
                        Text("Activa")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = dialog.canSave && !isSaving, onClick = onSave) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )

    if (scanning) {
        SkuScannerDialog(
            onDismiss = { scanning = false },
            onScanned = { code ->
                onChange(dialog.copy(sku = code))
                scanning = false
            },
        )
    }
}

/** Escáner a pantalla completa que devuelve el primer código leído. */
@Composable
private fun SkuScannerDialog(
    onDismiss: () -> Unit,
    onScanned: (String) -> Unit,
) {
    // Evita que los fotogramas siguientes vuelvan a disparar la callback
    var handled by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize()) {
            BarcodeScannerView(
                onBarcode = { code ->
                    if (!handled) {
                        handled = true
                        onScanned(code)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = "Apunta al código de barras",
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
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
            ) { Text("Cancelar") }
        }
    }
}


@Composable
private fun ProductImageThumb(
    url: String,
    esPrincipal: Boolean,
    onSetPrincipal: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (esPrincipal) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp),
                        ) else Modifier
                    )
                    .clickable { menuOpen = true },
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (!esPrincipal) {
                    DropdownMenuItem(
                        text = { Text("Hacer principal") },
                        onClick = { menuOpen = false; onSetPrincipal() },
                    )
                }
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = { menuOpen = false; onRemove() },
                )
            }
        }
        if (esPrincipal) {
            Text(
                "Principal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
