package com.passioagogo.market.ui.admin.customers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import com.passioagogo.market.data.customers.Customer
import com.passioagogo.market.data.customers.CustomerRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomersUiState(
    val customers: List<Customer> = emptyList(),
    val showInactive: Boolean = false,
    val editing: Customer? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val visible: List<Customer>
        get() = if (showInactive) customers else customers.filter { it.activo }
}

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onToggleInactive(show: Boolean) = _uiState.update { it.copy(showInactive = show) }
    fun onEdit(customer: Customer) = _uiState.update { it.copy(editing = customer) }
    fun onDismiss() = _uiState.update { it.copy(editing = null) }

    fun refresh() {
        viewModelScope.launch {
            val result = customerRepository.getCustomers(includeInactive = true)
            _uiState.update { state ->
                when (result) {
                    is DataResult.Success -> state.copy(customers = result.data)
                    is DataResult.Error ->
                        state.copy(errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onSave(nombre: String, telefono: String?, email: String?, notas: String?, activo: Boolean) {
        val editing = _uiState.value.editing ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = customerRepository.updateCustomer(
                editing.copy(
                    nombre = nombre,
                    telefono = telefono,
                    email = email,
                    notas = notas,
                    activo = activo,
                )
            )
            when (result) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, editing = null) }
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
fun CustomersScreen(viewModel: CustomersViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Clientes",
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
            items(state.visible, key = { it.id }) { customer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onEdit(customer) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(customer.nombre, style = MaterialTheme.typography.bodyLarge)
                        val detalle = listOfNotNull(customer.telefono, customer.email)
                            .joinToString("  ·  ")
                        if (detalle.isNotBlank()) {
                            Text(
                                detalle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (customer.profileId != null) {
                        Text(
                            "Con cuenta",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (!customer.activo) {
                        Text(
                            "  Inactivo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }

    state.editing?.let { customer ->
        var nombre by remember { mutableStateOf(customer.nombre) }
        var telefono by remember { mutableStateOf(customer.telefono ?: "") }
        var email by remember { mutableStateOf(customer.email ?: "") }
        var notas by remember { mutableStateOf(customer.notas ?: "") }
        var activo by remember { mutableStateOf(customer.activo) }

        AlertDialog(
            onDismissRequest = viewModel::onDismiss,
            title = { Text("Editar cliente") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre, onValueChange = { nombre = it },
                        label = { Text("Nombre") }, singleLine = true,
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
                        value = notas, onValueChange = { notas = it },
                        label = { Text("Notas") },
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = activo, onCheckedChange = { activo = it })
                        Text("Activo")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = nombre.isNotBlank() && !state.isSaving,
                    onClick = {
                        viewModel.onSave(
                            nombre.trim(),
                            telefono.ifBlank { null },
                            email.ifBlank { null },
                            notas.ifBlank { null },
                            activo,
                        )
                    },
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismiss) { Text("Cancelar") }
            },
        )
    }
}
