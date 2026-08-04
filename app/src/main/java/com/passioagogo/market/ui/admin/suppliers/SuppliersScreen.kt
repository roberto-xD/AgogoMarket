package com.passioagogo.market.ui.admin.suppliers

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
import androidx.compose.material3.FilterChip
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
import com.passioagogo.market.domain.purchases.Supplier
import com.passioagogo.market.domain.purchases.SupplierDraft
import com.passioagogo.market.domain.purchases.SupplierRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuppliersUiState(
    val suppliers: List<Supplier> = emptyList(),
    val showInactive: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val editing: Supplier? = null,
    val showDialog: Boolean = false,
) {
    val visible: List<Supplier>
        get() = if (showInactive) suppliers else suppliers.filter { it.activo }
}

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuppliersUiState())
    val uiState: StateFlow<SuppliersUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onToggleInactive(show: Boolean) = _uiState.update { it.copy(showInactive = show) }
    fun onNew() = _uiState.update { it.copy(showDialog = true, editing = null) }
    fun onEdit(supplier: Supplier) =
        _uiState.update { it.copy(showDialog = true, editing = supplier) }
    fun onDismiss() = _uiState.update { it.copy(showDialog = false, editing = null) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = supplierRepository.getSuppliers(includeInactive = true)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success ->
                        state.copy(isLoading = false, suppliers = result.data)
                    is DataResult.Error ->
                        state.copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onSave(draft: SupplierDraft, activo: Boolean) {
        val editing = _uiState.value.editing
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (editing == null) {
                supplierRepository.createSupplier(draft)
            } else {
                supplierRepository.updateSupplier(
                    editing.copy(
                        nombre = draft.nombre,
                        contacto = draft.contacto,
                        telefono = draft.telefono,
                        email = draft.email,
                        direccion = draft.direccion,
                        notas = draft.notas,
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
fun SuppliersScreen(viewModel: SuppliersViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Proveedores",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.showInactive,
                    onClick = { viewModel.onToggleInactive(!state.showInactive) },
                    label = { Text("Inactivos") },
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

            LazyColumn {
                items(state.visible, key = { it.id }) { supplier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEdit(supplier) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(supplier.nombre, style = MaterialTheme.typography.bodyLarge)
                            val detalle = listOfNotNull(
                                supplier.contacto,
                                supplier.telefono,
                            ).joinToString("  ·  ")
                            if (detalle.isNotBlank()) {
                                Text(
                                    detalle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (!supplier.activo) {
                            Text(
                                "Inactivo",
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
            Icon(Icons.Filled.Add, contentDescription = "Nuevo proveedor")
        }
    }

    if (state.showDialog) {
        SupplierDialog(
            editing = state.editing,
            isSaving = state.isSaving,
            onDismiss = viewModel::onDismiss,
            onSave = viewModel::onSave,
        )
    }
}

@Composable
private fun SupplierDialog(
    editing: Supplier?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (SupplierDraft, Boolean) -> Unit,
) {
    var nombre by remember { mutableStateOf(editing?.nombre ?: "") }
    var contacto by remember { mutableStateOf(editing?.contacto ?: "") }
    var telefono by remember { mutableStateOf(editing?.telefono ?: "") }
    var email by remember { mutableStateOf(editing?.email ?: "") }
    var direccion by remember { mutableStateOf(editing?.direccion ?: "") }
    var notas by remember { mutableStateOf(editing?.notas ?: "") }
    var activo by remember { mutableStateOf(editing?.activo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Nuevo proveedor" else "Editar proveedor") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre") }, singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contacto, onValueChange = { contacto = it },
                    label = { Text("Contacto") }, singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono") }, singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Correo") }, singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion, onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notas, onValueChange = { notas = it },
                    label = { Text("Notas") },
                )
                if (editing != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = activo, onCheckedChange = { activo = it })
                        Text("Activo")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && !isSaving,
                onClick = {
                    onSave(
                        SupplierDraft(
                            nombre = nombre.trim(),
                            contacto = contacto.ifBlank { null },
                            telefono = telefono.ifBlank { null },
                            email = email.ifBlank { null },
                            direccion = direccion.ifBlank { null },
                            notas = notas.ifBlank { null },
                        ),
                        activo,
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
