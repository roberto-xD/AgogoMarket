package com.passioagogo.market.ui.orders.shipping

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.LaunchedEffect
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
import com.passioagogo.market.data.customers.AddressDraft
import com.passioagogo.market.data.customers.Customer
import com.passioagogo.market.data.customers.CustomerDraft
import com.passioagogo.market.data.customers.CustomerRepository
import com.passioagogo.market.data.customers.ShippingAddress
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.sales.CartLine
import com.passioagogo.market.domain.sales.SalesRepository
import com.passioagogo.market.domain.sales.ShippingOrderDraft
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.transfers.VariantInfo
import com.passioagogo.market.ui.inventory.transfers.variantIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateShippingUiState(
    val customers: List<Customer> = emptyList(),
    val customerId: String? = null,
    val addresses: List<ShippingAddress> = emptyList(),
    val addressId: String? = null,
    val locations: List<Location> = emptyList(),
    val locationId: String? = null,
    val costoEnvio: String = "0",
    val notas: String = "",
    val query: String = "",
    val catalog: List<VariantInfo> = emptyList(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val lines: Map<String, Int> = emptyMap(),
    val showCustomerDialog: Boolean = false,
    val showAddressDialog: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdId: String? = null,
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
        get() = !isSaving && customerId != null && addressId != null &&
            locationId != null && lines.isNotEmpty() &&
            costoEnvio.toDoubleOrNull()?.let { it >= 0 } == true
}

@HiltViewModel
class CreateShippingViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val customerRepository: CustomerRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateShippingUiState())
    val uiState: StateFlow<CreateShippingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val customers = customerRepository.getCustomers()
            val locs = locationRepository.getLocations()
            val index = catalogRepository.variantIndex()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    customers = (customers as? DataResult.Success)?.data ?: emptyList(),
                    locations = (locs as? DataResult.Success)?.data ?: emptyList(),
                    catalog = index.values.sortedBy { it.producto },
                    variantIndex = index,
                    errorMessage = (customers as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    fun onCustomerSelected(id: String) {
        _uiState.update { it.copy(customerId = id, addressId = null, addresses = emptyList()) }
        viewModelScope.launch { loadAddresses(id) }
    }

    private suspend fun loadAddresses(customerId: String) {
        val result = customerRepository.getAddresses(customerId)
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(
                    addresses = result.data,
                    // Preselecciona la predeterminada
                    addressId = result.data.firstOrNull { it.esPredeterminada }?.id
                        ?: result.data.firstOrNull()?.id,
                )
                is DataResult.Error ->
                    state.copy(errorMessage = result.error.toMessage())
            }
        }
    }

    fun onAddressSelected(id: String) = _uiState.update { it.copy(addressId = id) }
    fun onLocationSelected(id: String) = _uiState.update { it.copy(locationId = id) }
    fun onCostoEnvioChange(v: String) = _uiState.update { it.copy(costoEnvio = v) }
    fun onNotasChange(v: String) = _uiState.update { it.copy(notas = v) }
    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }
    fun onOpenCustomerDialog() = _uiState.update { it.copy(showCustomerDialog = true) }
    fun onDismissCustomerDialog() = _uiState.update { it.copy(showCustomerDialog = false) }
    fun onOpenAddressDialog() = _uiState.update { it.copy(showAddressDialog = true) }
    fun onDismissAddressDialog() = _uiState.update { it.copy(showAddressDialog = false) }

    fun onAddVariant(variantId: String) = _uiState.update { state ->
        val current = state.lines[variantId] ?: 0
        state.copy(lines = state.lines + (variantId to current + 1), query = "")
    }

    fun onQuantityChange(variantId: String, cantidad: Int) = _uiState.update { state ->
        if (cantidad <= 0) state.copy(lines = state.lines - variantId)
        else state.copy(lines = state.lines + (variantId to cantidad))
    }

    fun onCreateCustomer(draft: CustomerDraft) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = customerRepository.createCustomer(draft)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showCustomerDialog = false,
                            customers = (it.customers + result.data).sortedBy { c -> c.nombre },
                        )
                    }
                    onCustomerSelected(result.data.id)
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onCreateAddress(draft: AddressDraft) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = customerRepository.createAddress(draft)) {
                is DataResult.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        showAddressDialog = false,
                        addresses = listOf(result.data) + it.addresses,
                        addressId = result.data.id,
                    )
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = salesRepository.createShippingOrder(
                ShippingOrderDraft(
                    customerId = state.customerId!!,
                    shippingAddressId = state.addressId!!,
                    locationId = state.locationId!!,
                    costoEnvio = state.costoEnvio.toDouble(),
                    lines = state.lines.map { (variantId, cantidad) ->
                        CartLine(variantId = variantId, cantidad = cantidad)
                    },
                    notas = state.notas.ifBlank { null },
                )
            )
            _uiState.update {
                when (result) {
                    is DataResult.Success ->
                        it.copy(isSaving = false, createdId = result.data.id)
                    is DataResult.Error ->
                        it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }
}

@Composable
fun CreateShippingScreen(
    onCreated: (String) -> Unit,
    viewModel: CreateShippingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.createdId) {
        state.createdId?.let { onCreated(it) }
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cliente
        Row(verticalAlignment = Alignment.CenterVertically) {
            PickerDropdown(
                label = "Cliente",
                options = state.customers.map { it.id to it.nombre },
                selectedId = state.customerId,
                onSelected = viewModel::onCustomerSelected,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::onOpenCustomerDialog) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo cliente")
            }
        }
        Spacer(Modifier.height(8.dp))

        // Dirección
        Row(verticalAlignment = Alignment.CenterVertically) {
            PickerDropdown(
                label = "Dirección de envío",
                options = state.addresses.map { it.id to it.resumen },
                selectedId = state.addressId,
                onSelected = viewModel::onAddressSelected,
                enabled = state.customerId != null,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = viewModel::onOpenAddressDialog,
                enabled = state.customerId != null,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva dirección")
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerDropdown(
                label = "Despachar desde",
                options = state.locations.map { it.id to it.nombre },
                selectedId = state.locationId,
                onSelected = viewModel::onLocationSelected,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.costoEnvio,
                onValueChange = viewModel::onCostoEnvioChange,
                label = { Text("Envío $") },
                singleLine = true,
                isError = state.costoEnvio.toDoubleOrNull() == null,
                modifier = Modifier.width(100.dp),
            )
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Agregar producto (nombre o SKU)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(Modifier.weight(1f)) {
            items(state.searchResults, key = { "s-${it.variantId}" }) { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAddVariant(info.variantId) }
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
                items(state.lines.entries.toList(), key = { "l-${it.key}" }) { (variantId, cantidad) ->
                    val info = state.variantIndex[variantId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(info?.producto ?: variantId)
                            Text(
                                info?.sku ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = cantidad.toString(),
                            onValueChange = { text ->
                                viewModel.onQuantityChange(variantId, text.toIntOrNull() ?: 0)
                            },
                            singleLine = true,
                            modifier = Modifier.width(80.dp),
                        )
                        IconButton(onClick = { viewModel.onQuantityChange(variantId, 0) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.notas,
            onValueChange = viewModel::onNotasChange,
            label = { Text("Notas") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

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
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSaving) "Creando…" else "Crear envío (descuenta stock)")
        }
    }

    if (state.showCustomerDialog) {
        CustomerDialog(
            isSaving = state.isSaving,
            onDismiss = viewModel::onDismissCustomerDialog,
            onSave = viewModel::onCreateCustomer,
        )
    }
    if (state.showAddressDialog && state.customerId != null) {
        AddressDialog(
            customerId = state.customerId!!,
            isSaving = state.isSaving,
            onDismiss = viewModel::onDismissAddressDialog,
            onSave = viewModel::onCreateAddress,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = modifier.menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelected(id)
                    },
                )
            }
        }
    }
}

@Composable
private fun CustomerDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (CustomerDraft) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo cliente") },
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
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && !isSaving,
                onClick = {
                    onSave(
                        CustomerDraft(
                            nombre = nombre.trim(),
                            telefono = telefono.ifBlank { null },
                            email = email.ifBlank { null },
                        )
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun AddressDialog(
    customerId: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (AddressDraft) -> Unit,
) {
    var alias by remember { mutableStateOf("") }
    var receptor by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var colonia by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var estadoDir by remember { mutableStateOf("") }
    var cp by remember { mutableStateOf("") }
    var referencias by remember { mutableStateOf("") }

    val valid = receptor.isNotBlank() && telefono.isNotBlank() && calle.isNotBlank() &&
        ciudad.isNotBlank() && estadoDir.isNotBlank() && cp.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva dirección") },
        text = {
            Column {
                OutlinedTextField(
                    value = alias, onValueChange = { alias = it },
                    label = { Text("Alias (Casa, Oficina…)") }, singleLine = true,
                )
                OutlinedTextField(
                    value = receptor, onValueChange = { receptor = it },
                    label = { Text("Recibe") }, singleLine = true,
                )
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono") }, singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calle, onValueChange = { calle = it },
                        label = { Text("Calle") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = numero, onValueChange = { numero = it },
                        label = { Text("Núm.") }, singleLine = true,
                        modifier = Modifier.width(90.dp),
                    )
                }
                OutlinedTextField(
                    value = colonia, onValueChange = { colonia = it },
                    label = { Text("Colonia") }, singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ciudad, onValueChange = { ciudad = it },
                        label = { Text("Ciudad") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = cp, onValueChange = { cp = it },
                        label = { Text("C.P.") }, singleLine = true,
                        modifier = Modifier.width(90.dp),
                    )
                }
                OutlinedTextField(
                    value = estadoDir, onValueChange = { estadoDir = it },
                    label = { Text("Estado") }, singleLine = true,
                )
                OutlinedTextField(
                    value = referencias, onValueChange = { referencias = it },
                    label = { Text("Referencias") }, singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !isSaving,
                onClick = {
                    onSave(
                        AddressDraft(
                            customerId = customerId,
                            alias = alias.ifBlank { null },
                            nombreReceptor = receptor.trim(),
                            telefono = telefono.trim(),
                            calle = calle.trim(),
                            numero = numero.ifBlank { null },
                            colonia = colonia.ifBlank { null },
                            ciudad = ciudad.trim(),
                            estado = estadoDir.trim(),
                            codigoPostal = cp.trim(),
                            referencias = referencias.ifBlank { null },
                        )
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
