package com.passioagogo.market.ui.admin.customers

import com.passioagogo.market.ui.common.OpcionesTeclado
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.width
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
import com.passioagogo.market.data.addresses.CryptoKeyRepository
import com.passioagogo.market.data.addresses.DireccionClara
import com.passioagogo.market.data.addresses.jsonDireccion
import com.passioagogo.market.data.customers.CustomerRepository
import com.passioagogo.market.data.users.ProfilesAdminRepository
import com.passioagogo.market.domain.auth.Profile
import com.passioagogo.market.domain.common.UserRole
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
    /** Perfiles con rol cliente: cuentas del sitio. */
    val perfilesCliente: List<Profile> = emptyList(),
    /** Coincidencias por correo pendientes de confirmar. */
    val sugerencias: List<SugerenciaEnlace> = emptyList(),
    val sugerenciaActiva: SugerenciaEnlace? = null,
    val showInactive: Boolean = false,
    val editing: Customer? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    // ---- Direcciones cifradas ----
    /** Cliente cuyas direcciones se están consultando. */
    val viendoDireccionesDe: ClienteUnificado? = null,
    val claveDesbloqueada: Boolean = false,
    val tieneArchivoClave: Boolean = false,
    val cargandoDirecciones: Boolean = false,
    /** Ya descifradas; se arman al abrir y se sueltan al cerrar. */
    val direcciones: List<DireccionDescifrada> = emptyList(),
    val errorClave: String? = null,
    /** Qué diálogo de clave mostrar; null = ninguno. */
    val pasoClave: PasoClave? = null,
    /** Direcciones ya guardadas: avisa antes de generar una clave nueva. */
    val direccionesGuardadas: Int = 0,
    val publicaGenerada: String? = null,
    val publicaRegistrada: Boolean = false,
) {
    /**
     * Las dos fuentes fusionadas.
     *
     * Una ficha enlazada aparece UNA vez, con los datos de mostrador y la
     * marca de cuenta. Un perfil sin ficha aparece como cliente sin ficha,
     * para que no quede invisible al buscar a quién venderle.
     */
    val visible: List<ClienteUnificado>
        get() {
            val enlazados = customers.mapNotNull { it.profileId }.toSet()

            val deMostrador = customers
                .filter { showInactive || it.activo }
                .map { c ->
                    val perfil = perfilesCliente.firstOrNull { it.id == c.profileId }
                    ClienteUnificado(
                        customer = c,
                        profileId = c.profileId,
                        profileNombre = perfil?.nombre,
                        nombre = c.nombre,
                        telefono = c.telefono,
                        email = c.email,
                        activo = c.activo,
                    )
                }

            val soloCuenta = perfilesCliente
                .filterNot { it.id in enlazados }
                .filter { showInactive || it.activo }
                .map { p ->
                    ClienteUnificado(
                        customer = null,
                        profileId = p.id,
                        profileNombre = p.nombre,
                        nombre = p.nombre,
                        telefono = null,
                        email = null,
                        activo = p.activo,
                    )
                }

            return (deMostrador + soloCuenta).sortedBy { it.nombre.lowercase() }
        }

    val conCuenta: Int get() = visible.count { it.tieneCuenta }
    val sinFicha: Int get() = visible.count { !it.tieneFicha }
}

/**
 * Un cliente, venga de donde venga.
 *
 * Fusiona las dos fuentes que antes vivían separadas: la ficha de
 * mostrador (`customers`) y la cuenta del sitio (`profiles` con rol
 * cliente). Pueden existir por separado o enlazadas.
 */
data class ClienteUnificado(
    /** Ficha de mostrador; null si solo tiene cuenta web. */
    val customer: Customer?,
    /** Cuenta del sitio; null si solo compró en mostrador. */
    val profileId: String?,
    val profileNombre: String?,
    val nombre: String,
    val telefono: String?,
    val email: String?,
    val activo: Boolean,
) {
    val tieneCuenta: Boolean get() = profileId != null
    val tieneFicha: Boolean get() = customer != null

    /** Solo quien tiene cuenta puede haber capturado direcciones cifradas. */
    val puedeVerDirecciones: Boolean get() = profileId != null

    val id: String get() = customer?.id ?: "perfil-$profileId"
}

/** Dos registros que parecen la misma persona, a la espera de confirmación. */
data class SugerenciaEnlace(
    val customer: Customer,
    val profileId: String,
    val profileNombre: String,
    val motivo: String,
)

/** Pasos del flujo de clave de administración. */
enum class PasoClave { FALTA_ARCHIVO, IMPORTAR, CREAR, CREADA, DESBLOQUEAR }

/** Una dirección lista para mostrar, o el motivo por el que no se pudo abrir. */
data class DireccionDescifrada(
    val id: String,
    val etiqueta: String?,
    val datos: DireccionClara?,
    /** Se envolvió con otra clave (rotación) o el bulto está corrupto. */
    val noSePudoAbrir: Boolean,
)

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository,
    private val adminKeyStore: AdminKeyStore,
    private val cryptoKeyRepository: CryptoKeyRepository,
    private val profilesRepository: ProfilesAdminRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        revisarClave()
        refresh()
    }

    /** Estado del archivo de la clave, para el indicador de la cabecera. */
    fun revisarClave() {
        _uiState.update {
            it.copy(
                tieneArchivoClave = adminKeyStore.tieneArchivo(),
                claveDesbloqueada = adminKeyStore.desbloqueada.value,
            )
        }
        viewModelScope.launch {
            val conteo = cryptoKeyRepository.contarPorClave()
            if (conteo is DataResult.Success) {
                _uiState.update { it.copy(direccionesGuardadas = conteo.data.values.sum()) }
            }
        }
    }

    // ---- Flujo de la clave ----

    fun onAbrirEstadoClave() = _uiState.update {
        it.copy(
            pasoClave = if (it.tieneArchivoClave) PasoClave.DESBLOQUEAR
            else PasoClave.FALTA_ARCHIVO,
            errorClave = null,
        )
    }

    fun onElegirImportar() = _uiState.update {
        it.copy(pasoClave = PasoClave.IMPORTAR, errorClave = null)
    }

    fun onElegirCrear() = _uiState.update {
        it.copy(pasoClave = PasoClave.CREAR, errorClave = null)
    }

    fun onCerrarPasoClave() = _uiState.update {
        // También se suelta el cliente en consulta: si se cancela el
        // desbloqueo, no debe quedar una ficha esperando abrirse.
        it.copy(
            pasoClave = null,
            errorClave = null,
            publicaGenerada = null,
            viendoDireccionesDe =
                if (it.claveDesbloqueada) it.viendoDireccionesDe else null,
        )
    }

    /**
     * Genera el par y registra la pública. Si el registro falla, la clave
     * ya existe en el dispositivo: se avisa para pegarla a mano en vez de
     * dejar el proceso a medias.
     */
    fun onCrearClave(contrasena: String, id: String) {
        _uiState.update { it.copy(isSaving = true, errorClave = null) }
        viewModelScope.launch {
            val par = adminKeyStore.generar(contrasena, id)
            if (par.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorClave = par.exceptionOrNull()?.message
                            ?: "No se pudo generar la clave",
                    )
                }
                return@launch
            }
            val generado = par.getOrThrow()
            // La nota ayuda al rotar: identifica dónde quedó la privada
            val registro = cryptoKeyRepository.registrar(
                id = generado.id,
                publica = generado.publicaB64,
                notas = "Generada desde la app el ${
                    java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("es", "MX"))
                        .format(java.util.Date())
                }",
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    pasoClave = PasoClave.CREADA,
                    tieneArchivoClave = true,
                    claveDesbloqueada = true,
                    publicaGenerada = generado.publicaB64,
                    publicaRegistrada = registro is DataResult.Success,
                )
            }
        }
    }

    /** Contenido para exportar como respaldo. */
    fun contenidoArchivoClave(): String? = adminKeyStore.contenidoArchivo()

    /**
     * Abre la clave con la contraseña y devuelve su pública.
     *
     * Deja la clave desbloqueada de paso: si la contraseña era correcta,
     * no tiene sentido volver a pedirla.
     */
    fun publicaClave(contrasena: String): String? {
        if (!adminKeyStore.desbloqueada.value) {
            val abierta = adminKeyStore.desbloquear(contrasena)
            if (abierta.isFailure) return null
            _uiState.update { it.copy(claveDesbloqueada = true) }
        }
        return adminKeyStore.publicaB64()
    }

    fun onOlvidarArchivo() {
        adminKeyStore.olvidarArchivo()
        revisarClave()
        _uiState.update { it.copy(pasoClave = null) }
    }

    fun onToggleInactive(show: Boolean) = _uiState.update { it.copy(showInactive = show) }
    // ============ Direcciones ============

    /**
     * Abre las direcciones de un cliente. Si la clave no está desbloqueada
     * pide primero la contraseña maestra.
     */
    /** Abre las direcciones de un cliente unificado, tenga ficha o no. */
    fun onVerDireccionesDe(cliente: ClienteUnificado) {
        val profileId = cliente.profileId ?: return
        _uiState.update {
            it.copy(
                viendoDireccionesDe = cliente,
                claveDesbloqueada = adminKeyStore.desbloqueada.value,
                tieneArchivoClave = adminKeyStore.tieneArchivo(),
                pasoClave = when {
                    adminKeyStore.desbloqueada.value -> null
                    adminKeyStore.tieneArchivo() -> PasoClave.DESBLOQUEAR
                    else -> PasoClave.FALTA_ARCHIVO
                },
                errorClave = null,
            )
        }
        if (adminKeyStore.desbloqueada.value) cargarDirecciones(profileId)
    }

    fun onCerrarDirecciones() = _uiState.update {
        // Las direcciones en claro no se conservan en memoria más de lo
        // necesario: se descartan al cerrar la ficha.
        it.copy(
            viendoDireccionesDe = null,
            direcciones = emptyList(),
            pasoClave = null,
            errorClave = null,
        )
    }

    fun onCancelarClave() = _uiState.update {
        it.copy(pasoClave = null, viendoDireccionesDe = null, errorClave = null)
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
                    pasoClave = null,
                )
            }
            _uiState.value.viendoDireccionesDe?.profileId?.let { cargarDirecciones(it) }
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

    /** El id del perfil es el mismo que `user_addresses.user_id`. */
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
            val fichas = customerRepository.getCustomers(includeInactive = true)
            val perfiles = profilesRepository.getProfiles()

            val listaFichas = (fichas as? DataResult.Success)?.data.orEmpty()
            val listaPerfiles = (perfiles as? DataResult.Success)?.data
                ?.filter { it.rol == UserRole.CLIENTE }
                .orEmpty()

            _uiState.update { state ->
                state.copy(
                    customers = listaFichas,
                    perfilesCliente = listaPerfiles,
                    sugerencias = detectarCoincidencias(listaFichas, listaPerfiles),
                    errorMessage = (fichas as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    /**
     * Busca fichas de mostrador y cuentas web que parezcan la misma
     * persona. Solo sugiere: enlazar dos historiales por error es peor que
     * dejarlos separados, así que lo confirma una persona.
     *
     * El correo es la única señal fiable; el nombre coincide demasiado a
     * menudo entre personas distintas como para proponerlo solo.
     */
    private fun detectarCoincidencias(
        fichas: List<Customer>,
        perfiles: List<Profile>,
    ): List<SugerenciaEnlace> {
        val yaEnlazados = fichas.mapNotNull { it.profileId }.toSet()
        val libres = perfiles.filterNot { it.id in yaEnlazados }
        if (libres.isEmpty()) return emptyList()

        return fichas
            .filter { it.profileId == null && !it.email.isNullOrBlank() }
            .mapNotNull { ficha ->
                val correo = ficha.email!!.trim().lowercase()
                // El perfil no guarda correo: se compara contra el nombre
                // cuando el correo de la ficha lo contiene, o por nombre
                // exacto como último recurso.
                val perfil = libres.firstOrNull { p ->
                    p.nombre.trim().equals(ficha.nombre.trim(), ignoreCase = true) ||
                        correo.substringBefore('@')
                            .equals(p.nombre.trim().lowercase().replace(" ", ""), true)
                } ?: return@mapNotNull null

                SugerenciaEnlace(
                    customer = ficha,
                    profileId = perfil.id,
                    profileNombre = perfil.nombre,
                    motivo = "Mismo nombre que una cuenta del sitio",
                )
            }
    }

    // ---- Enlace manual ----

    fun onRevisarSugerencia(s: SugerenciaEnlace) =
        _uiState.update { it.copy(sugerenciaActiva = s) }

    fun onDescartarSugerencia() = _uiState.update { it.copy(sugerenciaActiva = null) }

    fun onConfirmarEnlace() {
        val sugerencia = _uiState.value.sugerenciaActiva ?: return
        _uiState.update { it.copy(isSaving = true, sugerenciaActiva = null) }
        viewModelScope.launch {
            customerRepository.vincularPerfil(sugerencia.customer.id, sugerencia.profileId)
            _uiState.update { it.copy(isSaving = false) }
            refresh()
        }
    }

    fun onDesvincular(customer: Customer) {
        viewModelScope.launch {
            customerRepository.desvincularPerfil(customer.id)
            refresh()
        }
    }

    /**
     * Crea la ficha de quien solo tenía cuenta web. Sin ella, sus compras
     * quedarían fuera del historial por cliente.
     */
    fun onCrearFicha(cliente: ClienteUnificado) {
        val profileId = cliente.profileId ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val r = customerRepository.crearDesdePerfil(
                profileId = profileId,
                nombre = cliente.nombre,
                email = cliente.email,
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = (r as? DataResult.Error)?.error?.toMessage(),
                )
            }
            refresh()
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
    val contextoActual = LocalContext.current

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
        IndicadorClave(
            tieneArchivo = state.tieneArchivoClave,
            desbloqueada = state.claveDesbloqueada,
            onClick = viewModel::onAbrirEstadoClave,
        )

        // Posibles duplicados: se sugiere, nunca se une solo.
        if (state.sugerencias.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { viewModel.onRevisarSugerencia(state.sugerencias.first()) },
            ) {
                Text(
                    "${state.sugerencias.size} ficha(s) podrían ser la misma persona " +
                        "que una cuenta del sitio. Toca para revisarlas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        LazyColumn {
            items(state.visible, key = { it.id }) { cliente ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Sin ficha no hay nada que editar todavía
                            cliente.customer?.let(viewModel::onEdit)
                                ?: viewModel.onCrearFicha(cliente)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(cliente.nombre, style = MaterialTheme.typography.bodyLarge)
                        val detalle = listOfNotNull(cliente.telefono, cliente.email)
                            .joinToString("  ·  ")
                        if (detalle.isNotBlank()) {
                            Text(
                                detalle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        // De dónde viene cada cliente: sin esto no se
                        // distingue quién compró en mostrador de quién se
                        // registró en el sitio.
                        Text(
                            when {
                                cliente.tieneCuenta && cliente.tieneFicha ->
                                    "Ficha + cuenta del sitio"
                                cliente.tieneCuenta -> "Solo cuenta del sitio · toca para crear ficha"
                                else -> "Solo mostrador"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cliente.tieneCuenta) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    // Solo quien tiene cuenta en el sitio puede haber
                    // capturado una dirección cifrada.
                    if (cliente.puedeVerDirecciones) {
                        IconButton(
                            onClick = { viewModel.onVerDireccionesDe(cliente) },
                        ) {
                            Icon(
                                Icons.Outlined.Home,
                                contentDescription = "Ver direcciones",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (!cliente.activo) {
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

    val exportador = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val contenido = viewModel.contenidoArchivoClave()
        if (contenido != null) {
            runCatching {
                contextoActual.contentResolver.openOutputStream(uri)?.use {
                    it.write(contenido.toByteArray())
                }
            }
        }
        viewModel.onCerrarPasoClave()
    }

    state.sugerenciaActiva?.let { sugerencia ->
        AlertDialog(
            onDismissRequest = viewModel::onDescartarSugerencia,
            title = { Text("¿Es la misma persona?") },
            text = {
                Column {
                    Text(
                        "Ficha de mostrador: ${sugerencia.customer.nombre}" +
                            (sugerencia.customer.email?.let { "\n$it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cuenta del sitio: ${sugerencia.profileNombre}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Al enlazarlas compartirán historial y podrás ver las " +
                            "direcciones que capturó en el sitio. Si no estás " +
                            "seguro, déjalas separadas: unir a dos personas " +
                            "distintas es más difícil de deshacer que enlazarlas " +
                            "después.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmarEnlace) { Text("Sí, enlazar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDescartarSugerencia) {
                    Text("Son distintas")
                }
            },
        )
    }

    when (state.pasoClave) {
        PasoClave.FALTA_ARCHIVO -> FaltaClaveDialog(
            onImportar = viewModel::onElegirImportar,
            onCrear = viewModel::onElegirCrear,
            onDismiss = viewModel::onCerrarPasoClave,
        )
        PasoClave.IMPORTAR, PasoClave.DESBLOQUEAR -> DesbloquearClaveDialog(
            tieneArchivo = state.pasoClave == PasoClave.DESBLOQUEAR,
            isWorking = state.isSaving,
            errorMessage = state.errorClave,
            onDismiss = viewModel::onCerrarPasoClave,
            onDesbloquear = viewModel::onDesbloquear,
            onImportar = viewModel::onImportarClave,
            onCopiarPublica = viewModel::publicaClave,
        )
        PasoClave.CREAR -> CrearClaveDialog(
            direccionesExistentes = state.direccionesGuardadas,
            isWorking = state.isSaving,
            errorMessage = state.errorClave,
            onDismiss = viewModel::onCerrarPasoClave,
            onCrear = viewModel::onCrearClave,
        )
        PasoClave.CREADA -> ClaveCreadaDialog(
            publicaB64 = state.publicaGenerada.orEmpty(),
            registrada = state.publicaRegistrada,
            onExportar = { exportador.launch("admin-privada.json") },
            onDismiss = viewModel::onCerrarPasoClave,
        )
        null -> Unit
    }

    state.viendoDireccionesDe?.let { cliente ->
        if (state.claveDesbloqueada) {
            DireccionesSheet(
                nombreCliente = cliente.nombre,
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
                        keyboardOptions = OpcionesTeclado.Texto,
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
                        keyboardOptions = OpcionesTeclado.Texto,
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


/** Estado del archivo de la clave, visible de un vistazo. */
@Composable
private fun IndicadorClave(
    tieneArchivo: Boolean,
    desbloqueada: Boolean,
    onClick: () -> Unit,
) {
    val (color, contenedor, texto) = when {
        !tieneArchivo -> Triple(
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.errorContainer,
            "Falta la clave de administración: no podrás ver direcciones",
        )
        desbloqueada -> Triple(
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            "Clave abierta: puedes consultar direcciones",
        )
        else -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant,
            "Clave disponible: se pedirá la contraseña al abrir una dirección",
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = contenedor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (tieneArchivo) Icons.Filled.Lock else Icons.Filled.Warning,
                contentDescription = null,
                tint = color,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                texto,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Qué hacer cuando no hay archivo: importarlo (cambio de teléfono) o
 * crear uno nuevo (primera vez en este ambiente).
 */
@Composable
private fun FaltaClaveDialog(
    onImportar: () -> Unit,
    onCrear: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No se encontró la clave") },
        text = {
            Column {
                Text(
                    "Este dispositivo no tiene el archivo admin-privada.json, " +
                        "así que no puede descifrar direcciones.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Si ya tenías una clave (cambiaste de teléfono o reinstalaste " +
                        "la app), impórtala. Crea una nueva solo si es la primera " +
                        "vez que configuras este ambiente: una clave nueva no abre " +
                        "las direcciones guardadas con la anterior.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onImportar) { Text("Importar archivo") }
        },
        dismissButton = {
            TextButton(onClick = onCrear) { Text("Crear nueva") }
        },
    )
}
