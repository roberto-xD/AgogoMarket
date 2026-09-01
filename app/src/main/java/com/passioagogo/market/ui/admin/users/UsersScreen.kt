package com.passioagogo.market.ui.admin.users

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.users.ProfilesAdminRepository
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.Profile
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.common.UserRole
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal val UserRole.etiqueta: String
    get() = when (this) {
        UserRole.ADMIN -> "Administrador"
        UserRole.VENDEDOR -> "Vendedor"
        UserRole.CLIENTE -> "Cliente"
        UserRole.PROMOTOR -> "Promotor"
    }

data class UsersUiState(
    val currentUserId: String? = null,
    val profiles: List<Profile> = emptyList(),
    val locations: List<Location> = emptyList(),
    val editing: Profile? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    fun porRol(rol: UserRole): List<Profile> =
        profiles.filter { it.rol == rol }.sortedBy { it.nombre.lowercase() }

    /**
     * Vendedores agrupados por tienda. Los que no tienen asignación van al
     * final en su propio grupo: son los que no pueden operar y conviene
     * que salten a la vista.
     */
    val vendedoresPorTienda: List<Pair<String, List<Profile>>>
        get() {
            val vendedores = porRol(UserRole.VENDEDOR)
            val nombres = locations.associate { it.id to it.nombre }
            val conTienda = vendedores
                .filter { it.locationId != null }
                .groupBy { it.locationId!! }
                .map { (id, lista) -> (nombres[id] ?: "Ubicación desconocida") to lista }
                .sortedBy { it.first.lowercase() }
            val sinTienda = vendedores.filter { it.locationId == null }
            return if (sinTienda.isEmpty()) conTienda
            else conTienda + ("Sin tienda asignada" to sinTienda)
        }
}

/** Pestañas, en el orden pedido. */
private enum class PestanaUsuarios(val etiqueta: String, val rol: UserRole) {
    CLIENTES("Clientes", UserRole.CLIENTE),
    VENDEDORES("Vendedores", UserRole.VENDEDOR),
    PROMOTORES("Promotores", UserRole.PROMOTOR),
    ADMINS("Administradores", UserRole.ADMIN),
}

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val profilesRepository: ProfilesAdminRepository,
    private val locationRepository: LocationRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        val session = authRepository.sessionState.value as? SessionState.Authenticated
        _uiState.update { it.copy(currentUserId = session?.profile?.id) }
        viewModelScope.launch {
            val locs = locationRepository.getLocations()
            if (locs is DataResult.Success) {
                _uiState.update { it.copy(locations = locs.data) }
            }
            load()
        }
    }

    fun onEdit(profile: Profile) = _uiState.update { it.copy(editing = profile) }
    fun onDismiss() = _uiState.update { it.copy(editing = null) }
    fun refresh() = viewModelScope.launch { load() }

    fun onSave(nombre: String, rol: UserRole, locationId: String?, activo: Boolean) {
        val editing = _uiState.value.editing ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = profilesRepository.updateProfile(
                id = editing.id,
                nombre = nombre,
                rol = rol,
                locationId = locationId,
                activo = activo,
            )
            when (result) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, editing = null) }
                    load()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = profilesRepository.getProfiles()
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, profiles = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

@Composable
fun UsersScreen(viewModel: UsersViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val pestanas = PestanaUsuarios.entries

    Column(Modifier.fillMaxSize()) {
        // Desplazable: con cuatro rótulos largos no caben fijos en móvil
        ScrollableTabRow(selectedTabIndex = pestana, edgePadding = 8.dp) {
            pestanas.forEachIndexed { indice, p ->
                Tab(
                    selected = pestana == indice,
                    onClick = { pestana = indice },
                    text = {
                        val cuantos = state.porRol(p.rol).size
                        Text("${p.etiqueta} ($cuantos)")
                    },
                )
            }
        }

        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        }

        val seleccionada = pestanas[pestana]
        if (seleccionada == PestanaUsuarios.VENDEDORES) {
            val grupos = state.vendedoresPorTienda
            if (grupos.isEmpty()) {
                VacioUsuarios("Sin vendedores registrados")
            } else {
                LazyColumn {
                    grupos.forEach { (tienda, lista) ->
                        item(key = "h-$tienda") {
                            EncabezadoGrupo(
                                texto = tienda,
                                total = lista.size,
                                alerta = tienda == "Sin tienda asignada",
                            )
                        }
                        items(lista, key = { it.id }) { profile ->
                            FilaUsuario(
                                profile = profile,
                                esActual = profile.id == state.currentUserId,
                                // La tienda ya la dice el encabezado
                                tienda = null,
                                onClick = { viewModel.onEdit(profile) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        } else {
            val lista = state.porRol(seleccionada.rol)
            if (lista.isEmpty()) {
                VacioUsuarios("Sin ${seleccionada.etiqueta.lowercase()}")
            } else {
                LazyColumn {
                    items(lista, key = { it.id }) { profile ->
                        FilaUsuario(
                            profile = profile,
                            esActual = profile.id == state.currentUserId,
                            tienda = state.locations
                                .firstOrNull { it.id == profile.locationId }?.nombre,
                            onClick = { viewModel.onEdit(profile) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    state.editing?.let { profile ->
        UserDialog(
            profile = profile,
            locations = state.locations,
            esUsuarioActual = profile.id == state.currentUserId,
            isSaving = state.isSaving,
            onDismiss = viewModel::onDismiss,
            onSave = viewModel::onSave,
        )
    }
}

@Composable
private fun EncabezadoGrupo(texto: String, total: Int, alerta: Boolean) {
    Text(
        text = "$texto  ($total)",
        style = MaterialTheme.typography.labelLarge,
        color = if (alerta) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun FilaUsuario(
    profile: Profile,
    esActual: Boolean,
    tienda: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                profile.nombre + if (esActual) " (tú)" else "",
                style = MaterialTheme.typography.bodyLarge,
            )
            val detalle = listOfNotNull(profile.rol.etiqueta, tienda).joinToString("  ·  ")
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!profile.activo) {
            Text(
                "Inactivo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun VacioUsuarios(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDialog(
    profile: Profile,
    locations: List<Location>,
    esUsuarioActual: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, UserRole, String?, Boolean) -> Unit,
) {
    var nombre by remember { mutableStateOf(profile.nombre) }
    var rol by remember { mutableStateOf(profile.rol) }
    var locationId by remember { mutableStateOf(profile.locationId) }
    var activo by remember { mutableStateOf(profile.activo) }
    var rolExpanded by remember { mutableStateOf(false) }
    var locExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar usuario") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(rolExpanded, { rolExpanded = it }) {
                    OutlinedTextField(
                        value = rol.etiqueta,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(rolExpanded) },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(rolExpanded, { rolExpanded = false }) {
                        UserRole.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.etiqueta) },
                                onClick = { rol = r; rolExpanded = false },
                            )
                        }
                    }
                }
                // La tienda solo aplica a quien opera en una: el promotor no
                // tiene inventario y el cliente no opera. Al cambiar a esos
                // roles se limpia, para no dejar una asignación sin sentido.
                val usaTienda = rol == UserRole.ADMIN || rol == UserRole.VENDEDOR
                LaunchedEffect(rol) {
                    if (!usaTienda) locationId = null
                }

                if (usaTienda) {
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(locExpanded, { locExpanded = it }) {
                        OutlinedTextField(
                            value = locations.firstOrNull { it.id == locationId }?.nombre
                                ?: "Sin tienda",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tienda") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(locExpanded)
                            },
                            modifier = Modifier.menuAnchor(),
                        )
                        ExposedDropdownMenu(locExpanded, { locExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Sin tienda") },
                                onClick = { locationId = null; locExpanded = false },
                            )
                            locations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location.nombre) },
                                    onClick = { locationId = location.id; locExpanded = false },
                                )
                            }
                        }
                    }
                    if (rol == UserRole.VENDEDOR && locationId == null) {
                        Text(
                            "Un vendedor sin tienda no podrá operar el punto de venta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = activo,
                        onCheckedChange = { activo = it },
                        enabled = !esUsuarioActual,
                    )
                    Text(
                        if (esUsuarioActual) "Activo (no puedes desactivarte a ti mismo)"
                        else "Activo"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && !isSaving,
                onClick = { onSave(nombre.trim(), rol, locationId, activo) },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
