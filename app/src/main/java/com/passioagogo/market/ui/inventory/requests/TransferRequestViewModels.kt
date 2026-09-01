package com.passioagogo.market.ui.inventory.requests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.transferrequests.Disponibilidad
import com.passioagogo.market.data.transferrequests.TransferRequest
import com.passioagogo.market.data.transferrequests.TransferRequestDraft
import com.passioagogo.market.data.transferrequests.TransferRequestRepository
import com.passioagogo.market.data.transferrequests.TransferRequestStatus
import com.passioagogo.market.data.transferrequests.TransferRequestType
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.inventory.InventoryRepository
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.ui.common.toMessage
import com.passioagogo.market.ui.inventory.transfers.VariantInfo
import com.passioagogo.market.ui.inventory.transfers.variantIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal val TransferRequestStatus.etiqueta: String
    get() = when (this) {
        TransferRequestStatus.SOLICITADA -> "Pendiente"
        TransferRequestStatus.ACEPTADA -> "Aceptada"
        TransferRequestStatus.RECHAZADA -> "Rechazada"
        TransferRequestStatus.CANCELADA -> "Cancelada"
    }

internal val TransferRequestType.etiqueta: String
    get() = when (this) {
        TransferRequestType.PEDIDO -> "Pedido"
        TransferRequestType.DEVOLUCION -> "Devolución"
    }

// ============ Lista ============

data class TransferRequestsUiState(
    val openOnly: Boolean = true,
    val requests: List<TransferRequest> = emptyList(),
    val locationNames: Map<String, String> = emptyMap(),
    val miTienda: String? = null,
    val esAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    /** Distingue lo que me toca resolver de lo que yo pedí. */
    fun meTocaResolver(request: TransferRequest): Boolean {
        if (request.estado != TransferRequestStatus.SOLICITADA) return false
        val contraparte = when (request.tipo) {
            TransferRequestType.PEDIDO -> request.fromLocationId
            TransferRequestType.DEVOLUCION -> request.toLocationId
        }
        return esAdmin || (contraparte != null && contraparte == miTienda)
    }
}

@HiltViewModel
class TransferRequestsViewModel @Inject constructor(
    private val repository: TransferRequestRepository,
    private val locationRepository: LocationRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferRequestsUiState())
    val uiState: StateFlow<TransferRequestsUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value as? SessionState.Authenticated
        _uiState.update {
            it.copy(
                esAdmin = sesion?.isAdmin == true,
                miTienda = sesion?.profile?.locationId,
            )
        }
        viewModelScope.launch {
            val locs = locationRepository.getLocations(includeInactive = true)
            if (locs is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(locationNames = locs.data.associate { it.id to it.nombre })
                }
            }
            load()
        }
    }

    fun onToggleOpen(openOnly: Boolean) {
        _uiState.update { it.copy(openOnly = openOnly) }
        refresh()
    }

    fun refresh() = viewModelScope.launch { load() }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = repository.getRequests(openOnly = _uiState.value.openOnly)
        _uiState.update { state ->
            when (result) {
                is DataResult.Success -> state.copy(isLoading = false, requests = result.data)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

// ============ Creación ============

data class CreateRequestUiState(
    val tipo: TransferRequestType = TransferRequestType.PEDIDO,
    val locations: List<Location> = emptyList(),
    val miTienda: String? = null,
    val esAdmin: Boolean = false,
    /** Para un pedido: de dónde se pide. null = que decida el admin. */
    val fromLocationId: String? = null,
    /** Para una devolución: a dónde se devuelve. */
    val toLocationId: String? = null,
    val notas: String = "",
    val query: String = "",
    val catalog: List<VariantInfo> = emptyList(),
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val lines: Map<String, Int> = emptyMap(),
    /** variantId → dónde hay existencia. */
    val disponibilidad: Map<String, List<Disponibilidad>> = emptyMap(),
    /** Existencia en mi tienda, para no devolver más de lo que tengo. */
    val miStock: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val creada: Long? = null,
) {
    val searchResults: List<VariantInfo>
        get() {
            if (query.isBlank()) return emptyList()
            val q = query.trim().lowercase()
            val base = when (tipo) {
                // Se pide sobre TODO el catálogo: el sentido es pedir lo que
                // no se tiene, así que filtrar por stock local sería absurdo.
                TransferRequestType.PEDIDO -> catalog
                // Solo se devuelve lo que hay en existencia.
                TransferRequestType.DEVOLUCION -> catalog.filter {
                    (miStock[it.variantId] ?: 0) > 0
                }
            }
            return base.filter {
                it.producto.lowercase().contains(q) || it.sku.lowercase().contains(q)
            }.take(15)
        }

    /** Devolver más de lo que se tiene fallaría al enviar. */
    fun excedeExistencia(variantId: String, cantidad: Int): Boolean =
        tipo == TransferRequestType.DEVOLUCION && cantidad > (miStock[variantId] ?: 0)

    val hayExcesos: Boolean
        get() = lines.any { (id, cantidad) -> excedeExistencia(id, cantidad) }

    val canSave: Boolean
        get() = !isSaving && lines.isNotEmpty() && !hayExcesos && miTienda != null &&
            when (tipo) {
                TransferRequestType.PEDIDO -> fromLocationId != miTienda
                TransferRequestType.DEVOLUCION ->
                    toLocationId != null && toLocationId != miTienda
            }
}

@HiltViewModel
class CreateTransferRequestViewModel @Inject constructor(
    private val repository: TransferRequestRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    private val inventoryRepository: InventoryRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRequestUiState())
    val uiState: StateFlow<CreateRequestUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value as? SessionState.Authenticated
        val miTienda = sesion?.profile?.locationId
        _uiState.update {
            it.copy(esAdmin = sesion?.isAdmin == true, miTienda = miTienda)
        }

        viewModelScope.launch {
            val locs = locationRepository.getLocations()
            val index = catalogRepository.variantIndex()
            val stock = miTienda?.let { inventoryRepository.getStock(locationId = it) }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    locations = (locs as? DataResult.Success)?.data ?: emptyList(),
                    catalog = index.values.sortedBy { it.producto },
                    variantIndex = index,
                    miStock = (stock as? DataResult.Success)?.data
                        ?.associate { it.variantId to it.cantidad } ?: emptyMap(),
                    errorMessage = (locs as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    fun onTipo(tipo: TransferRequestType) = _uiState.update {
        // Cambiar de sentido invalida las líneas: no es lo mismo pedir que
        // devolver, y las existencias que aplican son distintas.
        it.copy(tipo = tipo, lines = emptyMap(), query = "", fromLocationId = null, toLocationId = null)
    }

    fun onFrom(id: String?) = _uiState.update { it.copy(fromLocationId = id) }
    fun onTo(id: String) = _uiState.update { it.copy(toLocationId = id) }
    fun onNotas(v: String) = _uiState.update { it.copy(notas = v) }
    fun onQuery(v: String) = _uiState.update { it.copy(query = v) }

    fun onAddVariant(variantId: String) {
        _uiState.update { state ->
            val actual = state.lines[variantId] ?: 0
            state.copy(lines = state.lines + (variantId to actual + 1), query = "")
        }
        cargarDisponibilidad()
    }

    fun onCantidad(variantId: String, cantidad: Int) = _uiState.update { state ->
        if (cantidad <= 0) state.copy(lines = state.lines - variantId)
        else state.copy(lines = state.lines + (variantId to cantidad))
    }

    /** Muestra qué ubicaciones tienen existencia de lo que se está pidiendo. */
    private fun cargarDisponibilidad() {
        val ids = _uiState.value.lines.keys.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val result = repository.getDisponibilidad(ids)
            if (result is DataResult.Success) {
                _uiState.update { state ->
                    state.copy(disponibilidad = result.data.groupBy { it.variantId })
                }
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return
        val miTienda = state.miTienda ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val draft = when (state.tipo) {
                TransferRequestType.PEDIDO -> TransferRequestDraft(
                    tipo = state.tipo,
                    fromLocationId = state.fromLocationId, // null = abierto
                    toLocationId = miTienda,
                    notas = state.notas.trim().ifBlank { null },
                    items = state.lines,
                )
                TransferRequestType.DEVOLUCION -> TransferRequestDraft(
                    tipo = state.tipo,
                    fromLocationId = miTienda,
                    toLocationId = state.toLocationId!!,
                    notas = state.notas.trim().ifBlank { null },
                    items = state.lines,
                )
            }
            val result = repository.createRequest(draft)
            _uiState.update {
                when (result) {
                    is DataResult.Success -> it.copy(isSaving = false, creada = result.data.folio)
                    is DataResult.Error ->
                        it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onDismissCreada() = _uiState.update { it.copy(creada = null) }
}

// ============ Detalle ============

data class RequestDetailUiState(
    val request: TransferRequest? = null,
    val variantIndex: Map<String, VariantInfo> = emptyMap(),
    val locationNames: Map<String, String> = emptyMap(),
    val locations: List<Location> = emptyList(),
    val esAdmin: Boolean = false,
    val miTienda: String? = null,
    val soyElSolicitante: Boolean = false,
    /** Origen elegido al aceptar un pedido abierto. */
    val origenElegido: String? = null,
    /** Cantidades a aprobar, editables. */
    val aprobadas: Map<String, String> = emptyMap(),
    /** Existencia del origen, para no aprobar más de lo que hay. */
    val stockOrigen: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val showRechazo: Boolean = false,
    val errorMessage: String? = null,
    val aceptada: Boolean = false,
) {
    val puedeResolver: Boolean
        get() {
            val r = request ?: return false
            if (r.estado != TransferRequestStatus.SOLICITADA) return false
            val contraparte = when (r.tipo) {
                TransferRequestType.PEDIDO -> r.fromLocationId
                TransferRequestType.DEVOLUCION -> r.toLocationId
            }
            // El admin resuelve como respaldo, igual que en el servidor
            return esAdmin || (contraparte != null && contraparte == miTienda)
        }

    val puedeCancelar: Boolean
        get() = soyElSolicitante && request?.estado == TransferRequestStatus.SOLICITADA

    /** Un pedido sin origen definido necesita que se elija al aceptar. */
    val requiereOrigen: Boolean
        get() = request?.tipo == TransferRequestType.PEDIDO && request.fromLocationId == null

    val origenEfectivo: String?
        get() = request?.fromLocationId ?: origenElegido

    fun excede(variantId: String): Boolean {
        val cantidad = aprobadas[variantId]?.toIntOrNull() ?: return false
        val disponible = stockOrigen[variantId] ?: 0
        return cantidad > disponible
    }

    val totalAprobado: Int
        get() = aprobadas.values.sumOf { it.toIntOrNull() ?: 0 }

    val puedeAceptar: Boolean
        get() = puedeResolver && !isProcessing && totalAprobado > 0 &&
            origenEfectivo != null &&
            aprobadas.keys.none { excede(it) }
}

@HiltViewModel
class TransferRequestDetailViewModel @Inject constructor(
    private val repository: TransferRequestRepository,
    private val locationRepository: LocationRepository,
    private val catalogRepository: CatalogRepository,
    private val inventoryRepository: InventoryRepository,
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])
    private val userId: String? =
        (authRepository.sessionState.value as? SessionState.Authenticated)?.profile?.id

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value as? SessionState.Authenticated
        _uiState.update {
            it.copy(esAdmin = sesion?.isAdmin == true, miTienda = sesion?.profile?.locationId)
        }
        viewModelScope.launch {
            _uiState.update { it.copy(variantIndex = catalogRepository.variantIndex()) }
            val locs = locationRepository.getLocations(includeInactive = true)
            if (locs is DataResult.Success) {
                _uiState.update { s ->
                    s.copy(
                        locations = locs.data.filter { it.activo },
                        locationNames = locs.data.associate { it.id to it.nombre },
                    )
                }
            }
            load()
        }
    }

    fun onOrigenElegido(id: String) {
        _uiState.update { it.copy(origenElegido = id) }
        viewModelScope.launch { cargarStockOrigen(id) }
    }

    fun onCantidadAprobada(variantId: String, valor: String) = _uiState.update {
        it.copy(aprobadas = it.aprobadas + (variantId to valor))
    }

    fun onAskRechazo() = _uiState.update { it.copy(showRechazo = true) }
    fun onDismissRechazo() = _uiState.update { it.copy(showRechazo = false) }

    fun onAceptar() {
        val state = _uiState.value
        if (!state.puedeAceptar) return
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
        viewModelScope.launch {
            val aprobadas = state.aprobadas.mapValues { (_, v) -> v.toIntOrNull() ?: 0 }
            val result = repository.aceptar(
                id = requestId,
                fromLocationId = if (state.requiereOrigen) state.origenElegido else null,
                aprobadas = aprobadas,
            )
            _uiState.update {
                when (result) {
                    is DataResult.Success -> it.copy(isProcessing = false, aceptada = true)
                    is DataResult.Error ->
                        it.copy(isProcessing = false, errorMessage = result.error.toMessage())
                }
            }
            if (result is DataResult.Success) load()
        }
    }

    fun onRechazar(motivo: String) {
        _uiState.update { it.copy(isProcessing = true, showRechazo = false) }
        viewModelScope.launch {
            when (val r = repository.rechazar(requestId, motivo)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isProcessing = false) }
                    load()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isProcessing = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onCancelar() {
        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            when (val r = repository.cancelar(requestId)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isProcessing = false) }
                    load()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isProcessing = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onDismissAceptada() = _uiState.update { it.copy(aceptada = false) }

    private suspend fun cargarStockOrigen(locationId: String) {
        val result = inventoryRepository.getStock(locationId = locationId)
        if (result is DataResult.Success) {
            _uiState.update { s ->
                s.copy(stockOrigen = result.data.associate { it.variantId to it.cantidad })
            }
        }
    }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true) }
        when (val result = repository.getRequest(requestId)) {
            is DataResult.Success -> {
                val request = result.data
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        request = request,
                        soyElSolicitante = request.solicitadoPor == userId,
                        // Se precarga lo pedido; el resolutor puede recortarlo
                        aprobadas = request.items.associate {
                            it.variantId to (it.cantidadAprobada ?: it.cantidadSolicitada).toString()
                        },
                    )
                }
                // Sin origen definido aún no hay existencia que comparar
                request.fromLocationId?.let { cargarStockOrigen(it) }
            }
            is DataResult.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}
