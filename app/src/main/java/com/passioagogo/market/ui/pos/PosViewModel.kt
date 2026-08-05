package com.passioagogo.market.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.preferences.UserPreferences
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.auth.AuthRepository
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.common.PaymentMethod
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationRepository
import com.passioagogo.market.domain.sales.CartLine
import com.passioagogo.market.domain.sales.CheckoutRequest
import com.passioagogo.market.domain.sales.Order
import com.passioagogo.market.domain.sales.PrecioVigente
import com.passioagogo.market.domain.sales.SalesRepository
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

/** Artículo vendible (variante aplanada del catálogo cacheado). */
data class PosItem(
    val variantId: String,
    val producto: String,
    val sku: String,
    val atributos: String,
    val precioLista: Double,
    val portada: String? = null,
)

data class CartEntry(
    val item: PosItem,
    val cantidad: Int,
    /** Precio del servidor con promo; null mientras carga. */
    val precio: PrecioVigente? = null,
) {
    val precioUnitario: Double get() = precio?.precioFinal ?: item.precioLista
    val importe: Double get() = precioUnitario * cantidad
    val tienePromo: Boolean get() = precio?.tienePromo == true
}

data class PosUiState(
    val isAdmin: Boolean = false,
    val bloqueadoSinTienda: Boolean = false,
    val locations: List<Location> = emptyList(),
    val locationId: String? = null,
    val query: String = "",
    val catalog: List<PosItem> = emptyList(),
    val cart: Map<String, CartEntry> = emptyMap(),
    val isCheckingOut: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val showScanner: Boolean = false,
    val scanMessage: String? = null,
    val errorMessage: String? = null,
    /** Venta completada: muestra el ticket. */
    val lastSale: Order? = null,
) {
    val searchResults: List<PosItem>
        get() {
            if (query.isBlank()) return emptyList()
            val q = query.trim().lowercase()
            return catalog.filter {
                it.producto.lowercase().contains(q) || it.sku.lowercase().contains(q)
            }.take(15)
        }

    /** Estimado del cliente; el total real lo calcula el servidor al cobrar. */
    val totalEstimado: Double get() = cart.values.sumOf { it.importe }

    val canCheckout: Boolean
        get() = !isCheckingOut && locationId != null && cart.isNotEmpty()
}

@HiltViewModel
class PosViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val catalogRepository: CatalogRepository,
    private val locationRepository: LocationRepository,
    private val userPreferences: UserPreferences,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        val session = authRepository.sessionState.value as? SessionState.Authenticated
        val isAdmin = session?.isAdmin == true
        _uiState.update {
            it.copy(
                isAdmin = isAdmin,
                bloqueadoSinTienda = session?.vendedorSinTienda == true,
            )
        }

        viewModelScope.launch {
            if (isAdmin) {
                val locs = locationRepository.getLocations()
                if (locs is DataResult.Success) {
                    _uiState.update { it.copy(locations = locs.data) }
                }
                // Última ubicación usada; solo si sigue existiendo
                val saved = userPreferences.activeLocationId.first()
                val valid = (locs as? DataResult.Success)?.data
                    ?.any { it.id == saved } == true
                _uiState.update { it.copy(locationId = if (valid) saved else null) }
            } else {
                _uiState.update { it.copy(locationId = session?.profile?.locationId) }
            }
        }

        // Catálogo vendible en vivo desde el caché
        viewModelScope.launch {
            catalogRepository.observeProducts().collect { products ->
                val items = products.flatMap { pw ->
                    pw.variants.map { v ->
                        PosItem(
                            variantId = v.id,
                            producto = pw.product.nombre,
                            sku = v.sku,
                            atributos = v.attributes.entries
                                .joinToString(" · ") { (k, value) ->
                                    "$k: ${value.jsonPrimitive.content}"
                                },
                            precioLista = v.precioVenta,
                            portada = pw.product.imagenes.firstOrNull(),
                        )
                    }
                }
                if (items.isEmpty()) catalogRepository.refreshCatalog()
                _uiState.update { it.copy(catalog = items) }
            }
        }
    }

    fun onLocationSelected(id: String) {
        _uiState.update { it.copy(locationId = id) }
        viewModelScope.launch { userPreferences.setActiveLocation(id) }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onAddItem(item: PosItem) {
        _uiState.update { state ->
            val current = state.cart[item.variantId]
            val entry = current?.copy(cantidad = current.cantidad + 1)
                ?: CartEntry(item = item, cantidad = 1)
            state.copy(cart = state.cart + (item.variantId to entry), query = "")
        }
        // Cotización con promo del servidor (solo la primera vez)
        if (_uiState.value.cart[item.variantId]?.precio == null) {
            viewModelScope.launch {
                val result = salesRepository.getPrecioVigente(item.variantId)
                if (result is DataResult.Success) {
                    _uiState.update { state ->
                        val entry = state.cart[item.variantId] ?: return@update state
                        state.copy(
                            cart = state.cart +
                                (item.variantId to entry.copy(precio = result.data))
                        )
                    }
                }
            }
        }
    }

    fun onQuantityChange(variantId: String, cantidad: Int) = _uiState.update { state ->
        if (cantidad <= 0) state.copy(cart = state.cart - variantId)
        else {
            val entry = state.cart[variantId] ?: return@update state
            state.copy(cart = state.cart + (variantId to entry.copy(cantidad = cantidad)))
        }
    }

    fun onOpenScanner() =
        _uiState.update { it.copy(showScanner = true, scanMessage = null) }

    fun onCloseScanner() =
        _uiState.update { it.copy(showScanner = false, scanMessage = null) }

    /** Cooldown por código: los frames consecutivos no duplican la línea. */
    private val scanCooldown = mutableMapOf<String, Long>()

    fun onBarcodeScanned(code: String) {
        val now = System.currentTimeMillis()
        val last = scanCooldown[code] ?: 0L
        if (now - last < 2_000) return
        scanCooldown[code] = now

        viewModelScope.launch {
            val variant = catalogRepository.findVariantBySku(code)
            when {
                variant == null -> _uiState.update {
                    it.copy(scanMessage = "SKU no encontrado: $code")
                }
                !variant.activo -> _uiState.update {
                    it.copy(scanMessage = "Variante inactiva: $code")
                }
                else -> {
                    val item = _uiState.value.catalog
                        .firstOrNull { it.variantId == variant.id }
                    if (item == null) {
                        _uiState.update {
                            it.copy(scanMessage = "Producto no disponible: $code")
                        }
                    } else {
                        onAddItem(item)
                        val cantidad = _uiState.value.cart[item.variantId]?.cantidad ?: 1
                        _uiState.update {
                            it.copy(scanMessage = "Agregado: ${item.producto} ×$cantidad")
                        }
                    }
                }
            }
        }
    }

    fun onOpenPayment() = _uiState.update { it.copy(showPaymentDialog = true) }
    fun onDismissPayment() = _uiState.update { it.copy(showPaymentDialog = false) }

    fun onCheckout(metodo: PaymentMethod, referencia: String?) {
        val state = _uiState.value
        if (!state.canCheckout) return
        _uiState.update {
            it.copy(isCheckingOut = true, showPaymentDialog = false, errorMessage = null)
        }
        viewModelScope.launch {
            val result = salesRepository.checkout(
                CheckoutRequest(
                    locationId = state.locationId!!,
                    lines = state.cart.values.map {
                        CartLine(variantId = it.item.variantId, cantidad = it.cantidad)
                    },
                    metodo = metodo,
                    referencia = referencia?.ifBlank { null },
                )
            )
            _uiState.update {
                when (result) {
                    is DataResult.Success ->
                        it.copy(isCheckingOut = false, cart = emptyMap(), lastSale = result.data)
                    is DataResult.Error ->
                        it.copy(isCheckingOut = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    /** Cierra el ticket y prepara la siguiente venta. */
    fun onNewSale() = _uiState.update { it.copy(lastSale = null, errorMessage = null) }
}
