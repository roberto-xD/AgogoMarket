package com.passioagogo.market.ui.admin.customers

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.customers.Customer
import com.passioagogo.market.core.crypto.AdminKeyStore
import com.passioagogo.market.data.addresses.AddressRepository
import com.passioagogo.market.data.addresses.DireccionClara
import com.passioagogo.market.data.addresses.jsonDireccion
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
    // ---- Direcciones cifradas ----
    /** Cliente cuyas direcciones se están consultando. */
    val viendoDirecciones: Customer? = null,
    val claveDesbloqueada: Boolean = false,
    val tieneArchivoClave: Boolean = false,
    val pidiendoClave: Boolean = false,
    val cargandoDirecciones: Boolean = false,
    /** Ya descifradas; se arman al abrir y se sueltan al cerrar. */
    val direcciones: List<DireccionDescifrada> = emptyList(),
    val errorClave: String? = null,
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
    // ============ Direcciones ============

    /**
     * Abre las direcciones de un cliente. Si la clave no está desbloqueada
     * pide primero la contraseña maestra.
     */
    fun onVerDirecciones(customer: Customer) {
        if (customer.profileId == null) {
            _uiState.update {
                it.copy(errorMessage = "Este cliente no tiene cuenta en el sitio")
            }
            return
        }
        _uiState.update {
            it.copy(
                viendoDirecciones = customer,
                claveDesbloqueada = adminKeyStore.desbloqueada.value,
                tieneArchivoClave = adminKeyStore.tieneArchivo(),
                pidiendoClave = !adminKeyStore.desbloqueada.value,
                errorClave = null,
            )
        }
        if (adminKeyStore.desbloqueada.value) cargarDirecciones(customer.profileId)
    }

    fun onCerrarDirecciones() = _uiState.update {
        // Las direcciones en claro no se conservan en memoria más de lo
        // necesario: se descartan al cerrar la ficha.
        it.copy(
            viendoDirecciones = null,
            direcciones = emptyList(),
            pidiendoClave = false,
            errorClave = null,
        )
    }

    fun onCancelarClave() = _uiState.update {
        it.copy(pidiendoClave = false, viendoDirecciones = null, errorClave = null)
    }

    fun onDesbloquear(contrasena: String) {
        _uiState.update { it.copy(isSaving = true, errorClave = null) }
        val resultado = adminKeyStore.desbloquear(contrasena)
        aplicarResultadoClave(resultado)
    }

    fun onImportarClave(contenidoJson: String, contrasena: String) {
        _uiState.update { it.copy(isSaving = true, errorClave = null) }
        val resultado = adminKeyStore.importar(contenidoJson, contrasena)
        aplicarResultadoClave(resultado)
    }

    private fun aplicarResultadoClave(resultado: Result<Unit>) {
        if (resultado.isSuccess) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    claveDesbloqueada = true,
                    tieneArchivoClave = true,
                    pidiendoClave = false,
                )
            }
            _uiState.value.viendoDirecciones?.profileId?.let { cargarDirecciones(it) }
        } else {
            // Mensaje único a propósito: distinguir «contraseña mala» de
            // «archivo corrupto» ayudaría a quien prueba contraseñas.
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorClave = "No se pudo abrir con esos datos. Revisa la " +
                        "contraseña y el archivo.",
                )
            }
        }
    }

    private fun cargarDirecciones(profileId: String) {
        _uiState.update { it.copy(cargandoDirecciones = true) }
        viewModelScope.launch {
            val result = addressRepository.getDirecciones(profileId)
            val descifradas = (result as? DataResult.Success)?.data.orEmpty().map { dir ->
                val claro = adminKeyStore.descifrar(dir.contenido, dir.iv, dir.dkAdmin)
                DireccionDescifrada(
                    id = dir.id,
                    etiqueta = dir.etiqueta,
                    datos = claro?.let {
                        runCatching { jsonDireccion.decodeFromString<DireccionClara>(it) }
                            .getOrNull()
                    },
                    noSePudoAbrir = claro == null,
                )
            }
            _uiState.update {
                it.copy(
                    cargandoDirecciones = false,
                    direcciones = descifradas,
                    errorMessage = (result as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

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
fun CustomersScreen(
    onBack: ()-> Unit,
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        onBack()
    }
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
                    // Solo quien tiene cuenta en el sitio puede haber
                    // capturado una dirección cifrada.
                    if (customer.profileId != null) {
                        IconButton(onClick = { viewModel.onVerDirecciones(customer) }) {
                            Icon(
                                Icons.Outlined.Home,
                                contentDescription = "Ver direcciones",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
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

    if (state.pidiendoClave) {
        DesbloquearClaveDialog(
            tieneArchivo = state.tieneArchivoClave,
            isWorking = state.isSaving,
            errorMessage = state.errorClave,
            onDismiss = viewModel::onCancelarClave,
            onDesbloquear = viewModel::onDesbloquear,
            onImportar = viewModel::onImportarClave,
        )
    }

    state.viendoDirecciones?.let { customer ->
        if (state.claveDesbloqueada) {
            DireccionesSheet(
                customer = customer,
                direcciones = state.direcciones,
                cargando = state.cargandoDirecciones,
                onDismiss = viewModel::onCerrarDirecciones,
            )
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
