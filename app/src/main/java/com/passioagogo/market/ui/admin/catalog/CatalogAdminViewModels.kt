package com.passioagogo.market.ui.admin.catalog

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.images.ImageCompressor
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.sku.SkuGenerator
import com.passioagogo.market.data.catalog.ProductImageRepository
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.catalog.Category
import com.passioagogo.market.domain.catalog.CategoryDraft
import com.passioagogo.market.domain.catalog.Product
import com.passioagogo.market.domain.catalog.ProductDraft
import com.passioagogo.market.domain.catalog.ProductVariant
import com.passioagogo.market.domain.catalog.ProductWithVariants
import com.passioagogo.market.domain.catalog.VariantDraft
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

// ---------- attributes jsonb ⇆ texto "clave: valor" ----------

fun JsonObject.toAttributesText(): String =
    entries.joinToString("\n") { (k, v) -> "$k: ${v.jsonPrimitive.content}" }

fun String.toAttributesJson(): JsonObject = JsonObject(
    lines()
        .mapNotNull { line ->
            val idx = line.indexOf(':')
            if (idx <= 0) null
            else {
                val key = line.take(idx).trim()
                val value = line.drop(idx + 1).trim()
                if (key.isEmpty()) null else key to JsonPrimitive(value)
            }
        }
        .toMap()
)

// ============ Lista (productos + categorías) ============

data class CatalogAdminUiState(
    val products: List<ProductWithVariants> = emptyList(),
    val categories: List<Category> = emptyList(),
    val query: String = "",
    val showInactive: Boolean = false,
    /**
     * Consulta: sin edición ni altas. Es el modo obligado del promotor y
     * opcional para vendedor y admin, que lo usan para mostrar producto.
     */
    val modoConsulta: Boolean = false,
    /** true si el rol no puede editar el catálogo en ningún caso. */
    val soloLectura: Boolean = false,
    /**
     * El cliente solo navega productos: las categorías no filtran nada en
     * esta pantalla, así que su pestaña no le aporta.
     */
    val soloProductos: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** Categoría en edición en el diálogo (null = diálogo cerrado). */
    val editingCategory: Category? = null,
    val showCategoryDialog: Boolean = false,
    val isSavingCategory: Boolean = false,
) {
    val filteredProducts: List<ProductWithVariants>
        get() {
            val base = when {
                // En consulta se muestran los activos y los que se consiguen
                // por encargo; nunca los descatalogados.
                modoConsulta -> products.filter { it.product.activo || it.product.sobrePedido }
                showInactive -> products
                else -> products.filter { it.product.activo }
            }
            if (query.isBlank()) return base
            val q = query.trim().lowercase()
            return base.filter { pw ->
                pw.product.nombre.lowercase().contains(q) ||
                    (pw.product.marca?.lowercase()?.contains(q) == true) ||
                    pw.variants.any { it.sku.lowercase().contains(q) }
            }
        }

    val visibleCategories: List<Category>
        get() = if (showInactive) categories else categories.filter { it.activo }
}

@HiltViewModel
class CatalogAdminViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    authRepository: com.passioagogo.market.domain.auth.AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogAdminUiState())
    val uiState: StateFlow<CatalogAdminUiState> = _uiState.asStateFlow()

    init {
        val sesion = authRepository.sessionState.value
            as? com.passioagogo.market.domain.auth.SessionState.Authenticated
        val puedeEditar = sesion?.isAdmin == true
        val esCliente = sesion?.isCliente == true
        _uiState.update {
            it.copy(
                soloLectura = !puedeEditar,
                modoConsulta = !puedeEditar,
                soloProductos = esCliente,
            )
        }

        viewModelScope.launch {
            catalogRepository.observeAllProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
        viewModelScope.launch {
            catalogRepository.observeCategories(includeInactive = true).collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        refresh()
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }
    fun onToggleInactive(show: Boolean) = _uiState.update { it.copy(showInactive = show) }

    fun onToggleConsulta(activo: Boolean) = _uiState.update {
        if (it.soloLectura) it else it.copy(modoConsulta = activo)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            val result = catalogRepository.refreshCatalog()
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    errorMessage = (result as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    // ---------- Categorías (diálogo) ----------

    fun onNewCategory() =
        _uiState.update { it.copy(showCategoryDialog = true, editingCategory = null) }

    fun onEditCategory(category: Category) =
        _uiState.update { it.copy(showCategoryDialog = true, editingCategory = category) }

    fun onDismissCategoryDialog() =
        _uiState.update { it.copy(showCategoryDialog = false, editingCategory = null) }

    fun onSaveCategory(
        nombre: String,
        descripcion: String?,
        parentId: String?,
        activo: Boolean,
    ) {
        val editing = _uiState.value.editingCategory
        _uiState.update { it.copy(isSavingCategory = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (editing == null) {
                catalogRepository.createCategory(
                    CategoryDraft(nombre = nombre, parentId = parentId, descripcion = descripcion)
                )
            } else {
                catalogRepository.updateCategory(
                    editing.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        parentId = parentId,
                        activo = activo,
                    )
                )
            }
            _uiState.update {
                when (result) {
                    is DataResult.Success -> it.copy(
                        isSavingCategory = false,
                        showCategoryDialog = false,
                        editingCategory = null,
                    )
                    is DataResult.Error -> it.copy(
                        isSavingCategory = false,
                        errorMessage = result.error.toMessage(),
                    )
                }
            }
        }
    }
}

// ============ Edición de producto ============

data class VariantDialogState(
    /** null = creando */
    val editing: ProductVariant? = null,
    val sku: String = "",
    val precio: String = "",
    val costo: String = "",
    val attributesText: String = "",
    val activo: Boolean = true,
) {
    val canSave: Boolean
        get() = sku.isNotBlank() && precio.toDoubleOrNull() != null &&
            (costo.isBlank() || costo.toDoubleOrNull() != null)
}

data class ProductEditUiState(
    /** null hasta que se crea (modo alta). */
    val productId: String? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val resumen: String = "",
    val sobrePedido: Boolean = false,
    val marca: String = "",
    val categoryId: String? = null,
    val attributesText: String = "",
    val activo: Boolean = true,
    val imagenes: List<String> = emptyList(),
    val isUploadingImages: Boolean = false,
    val categories: List<Category> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val variantDialog: VariantDialogState? = null,
) {
    val isNew: Boolean get() = productId == null
    val canSave: Boolean get() = !isSaving && nombre.isNotBlank() && categoryId != null
    /** El CHECK del servidor limita el resumen a 160 caracteres. */
    val resumenValido: Boolean get() = resumen.length <= 160
}

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val productImageRepository: ProductImageRepository,
    private val imageCompressor: ImageCompressor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialProductId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    /** Evita reintentar el refresco en bucle si el catálogo está realmente vacío. */
    private var refrescoIntentado = false

    init {
        // Reactivo, no una lectura única: si el caché aún no tenía categorías
        // cuando se abrió la pantalla, el desplegable se puebla al llegar.
        viewModelScope.launch {
            catalogRepository.observeCategories(includeInactive = false).collect { cats ->
                _uiState.update { it.copy(categories = cats) }
                if (cats.isEmpty() && !refrescoIntentado) {
                    refrescoIntentado = true
                    catalogRepository.refreshCatalog()
                }
            }
        }

        viewModelScope.launch {
            if (initialProductId != null) {
                val loaded = catalogRepository.observeProduct(initialProductId).first()
                if (loaded != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            productId = loaded.product.id,
                            nombre = loaded.product.nombre,
                            descripcion = loaded.product.descripcion.orEmpty(),
                            resumen = loaded.product.resumen.orEmpty(),
                            sobrePedido = loaded.product.sobrePedido,
                            marca = loaded.product.marca.orEmpty(),
                            categoryId = loaded.product.categoryId,
                            attributesText = loaded.product.attributes.toAttributesText(),
                            activo = loaded.product.activo,
                            imagenes = loaded.product.imagenes,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Producto no encontrado")
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }

            // Variantes en vivo desde el caché (refleja altas/ediciones)
            initialProductId?.let { observeVariants(it) }
        }
    }

    private fun observeVariants(productId: String) {
        viewModelScope.launch {
            catalogRepository.observeAllProducts().collect { products ->
                val variants = products
                    .firstOrNull { it.product.id == productId }?.variants ?: emptyList()
                _uiState.update { it.copy(variants = variants) }
            }
        }
    }

    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun onResumenChange(v: String) = _uiState.update { it.copy(resumen = v) }
    fun onSobrePedidoChange(v: Boolean) = _uiState.update { it.copy(sobrePedido = v) }
    fun onMarcaChange(v: String) = _uiState.update { it.copy(marca = v) }
    fun onCategorySelected(id: String) = _uiState.update { it.copy(categoryId = id) }
    fun onAttributesChange(v: String) = _uiState.update { it.copy(attributesText = v) }
    fun onActivoChange(v: Boolean) = _uiState.update {
        if (v) it.copy(activo = true, sobrePedido = false) else it.copy(activo = false)
    }

    fun onSaveProduct() {
        val state = _uiState.value
        if (state.isSaving) return

        // El botón permanece habilitado a propósito: un botón inerte sin
        // explicación deja al usuario sin saber qué falta.
        val problema = when {
            state.nombre.isBlank() -> "Escribe el nombre del producto"
            state.categories.isEmpty() ->
                "No hay categorías activas. Crea una en Catálogo → Categorías."
            state.categoryId == null -> "Elige una categoría"
            !state.resumenValido -> "El resumen no puede pasar de 160 caracteres"
            else -> null
        }
        if (problema != null) {
            _uiState.update { it.copy(errorMessage = problema) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (state.isNew) {
                catalogRepository.createProduct(
                    ProductDraft(
                        nombre = state.nombre.trim(),
                        descripcion = state.descripcion.ifBlank { null },
                        resumen = state.resumen.trim().ifBlank { null },
                        categoryId = state.categoryId!!,
                        marca = state.marca.ifBlank { null },
                        attributes = state.attributesText.toAttributesJson(),
                    )
                )
            } else {
                catalogRepository.updateProduct(
                    Product(
                        id = state.productId!!,
                        nombre = state.nombre.trim(),
                        descripcion = state.descripcion.ifBlank { null },
                        resumen = state.resumen.trim().ifBlank { null },
                        sobrePedido = state.sobrePedido,
                        categoryId = state.categoryId!!,
                        marca = state.marca.ifBlank { null },
                        attributes = state.attributesText.toAttributesJson(),
                        imagenes = state.imagenes,
                        activo = state.activo,
                    )
                )
            }
            _uiState.update {
                when (result) {
                    is DataResult.Success -> {
                        val id = result.data.id
                        if (it.isNew) observeVariants(id)
                        it.copy(isSaving = false, productId = id)
                    }
                    is DataResult.Error ->
                        it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    // ---------- Variantes ----------

    fun onNewVariant() = _uiState.update { it.copy(variantDialog = VariantDialogState()) }

    fun onEditVariant(variant: ProductVariant) = _uiState.update {
        it.copy(
            variantDialog = VariantDialogState(
                editing = variant,
                sku = variant.sku,
                precio = variant.precioVenta.toString(),
                costo = variant.costo.toString(),
                attributesText = variant.attributes.toAttributesText(),
                activo = variant.activo,
            )
        )
    }

    fun onDismissVariantDialog() = _uiState.update { it.copy(variantDialog = null) }

    /**
     * Propone un SKU libre. Comprueba contra el caché para no chocar con uno
     * existente; la restricción UNIQUE de la columna es la garantía final.
     */
    fun onGenerateSku() {
        val nombre = _uiState.value.nombre.ifBlank { "Producto" }
        viewModelScope.launch {
            var candidato = SkuGenerator.generate(nombre)
            var intentos = 0
            while (catalogRepository.findVariantBySku(candidato) != null && intentos < 5) {
                candidato = SkuGenerator.generate(nombre)
                intentos++
            }
            _uiState.update { state ->
                state.copy(variantDialog = state.variantDialog?.copy(sku = candidato))
            }
        }
    }

    fun onVariantDialogChange(dialog: VariantDialogState) =
        _uiState.update { it.copy(variantDialog = dialog) }

    fun onSaveVariant() {
        val state = _uiState.value
        val dialog = state.variantDialog ?: return
        val productId = state.productId ?: return
        if (!dialog.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val editing = dialog.editing
            val result = if (editing == null) {
                catalogRepository.createVariant(
                    VariantDraft(
                        productId = productId,
                        sku = dialog.sku.trim(),
                        attributes = dialog.attributesText.toAttributesJson(),
                        precioVenta = dialog.precio.toDouble(),
                        costo = dialog.costo.toDoubleOrNull() ?: 0.0,
                    )
                )
            } else {
                catalogRepository.updateVariant(
                    editing.copy(
                        sku = dialog.sku.trim(),
                        attributes = dialog.attributesText.toAttributesJson(),
                        precioVenta = dialog.precio.toDouble(),
                        costo = dialog.costo.toDoubleOrNull() ?: 0.0,
                        activo = dialog.activo,
                    )
                )
            }
            _uiState.update {
                when (result) {
                    is DataResult.Success ->
                        it.copy(isSaving = false, variantDialog = null)
                    is DataResult.Error -> {
                        val mensaje = result.error.toMessage()
                        it.copy(
                            isSaving = false,
                            errorMessage = if (mensaje.contains("product_variants_sku_key") ||
                                mensaje.contains("duplicate key")
                            ) {
                                "Ese SKU ya está en uso por otra variante"
                            } else {
                                mensaje
                            },
                        )
                    }
                }
            }
        }
    }

    // ---------- Imágenes ----------

    fun onImagesPicked(uris: List<Uri>) {
        val productId = _uiState.value.productId ?: return
        if (uris.isEmpty()) return
        _uiState.update { it.copy(isUploadingImages = true, errorMessage = null) }
        viewModelScope.launch {
            val nuevas = mutableListOf<String>()
            var error: String? = null
            for (uri in uris.take(5)) {
                val bytes = imageCompressor.compress(uri).getOrElse { e ->
                    // Se expone la causa real: formato no soportado, sin
                    // memoria, permiso de lectura revocado, etc.
                    error = "No se pudo procesar la imagen: " +
                        (e.message ?: e::class.simpleName ?: "error desconocido")
                    null
                } ?: continue
                when (val result = productImageRepository.upload(productId, bytes)) {
                    is DataResult.Success -> nuevas += result.data
                    is DataResult.Error -> error = result.error.toMessage()
                }
            }
            if (nuevas.isNotEmpty()) {
                persistImagenes(_uiState.value.imagenes + nuevas, uploadError = error)
            } else {
                _uiState.update { it.copy(isUploadingImages = false, errorMessage = error) }
            }
        }
    }

    fun onRemoveImage(url: String) {
        viewModelScope.launch {
            productImageRepository.delete(url) // best-effort
            persistImagenes(_uiState.value.imagenes - url)
        }
    }

    fun onSetPrincipal(url: String) {
        val current = _uiState.value.imagenes
        if (current.firstOrNull() == url) return
        viewModelScope.launch {
            persistImagenes(listOf(url) + (current - url))
        }
    }

    /** Guarda la lista de imágenes en el producto y sincroniza el estado. */
    private suspend fun persistImagenes(imagenes: List<String>, uploadError: String? = null) {
        val state = _uiState.value
        val productId = state.productId ?: return
        val result = catalogRepository.updateProduct(
            Product(
                id = productId,
                nombre = state.nombre.trim(),
                descripcion = state.descripcion.ifBlank { null },
                resumen = state.resumen.trim().ifBlank { null },
                sobrePedido = state.sobrePedido,
                categoryId = state.categoryId ?: return,
                marca = state.marca.ifBlank { null },
                attributes = state.attributesText.toAttributesJson(),
                imagenes = imagenes,
                activo = state.activo,
            )
        )
        _uiState.update {
            when (result) {
                is DataResult.Success -> it.copy(
                    isUploadingImages = false,
                    imagenes = result.data.imagenes,
                    errorMessage = uploadError,
                )
                is DataResult.Error -> it.copy(
                    isUploadingImages = false,
                    errorMessage = result.error.toMessage(),
                )
            }
        }
    }
}
