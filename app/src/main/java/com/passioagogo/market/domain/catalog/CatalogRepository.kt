package com.passioagogo.market.domain.catalog

import com.passioagogo.market.core.result.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

// ---------- Modelos de dominio ----------

data class Category(
    val id: String,
    val nombre: String,
    val parentId: String?,
    val descripcion: String?,
    val activo: Boolean,
)

data class Product(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val categoryId: String,
    val marca: String?,
    val attributes: JsonObject,
    val imagenes: List<String>,
    val activo: Boolean,
)

data class ProductVariant(
    val id: String,
    val productId: String,
    val sku: String,
    val attributes: JsonObject,
    val precioVenta: Double,
    val costo: Double,
    val activo: Boolean,
)

data class ProductWithVariants(
    val product: Product,
    val variants: List<ProductVariant>,
)

// ---------- Comandos de escritura (admin) ----------

data class CategoryDraft(
    val nombre: String,
    val parentId: String? = null,
    val descripcion: String? = null,
)

data class ProductDraft(
    val nombre: String,
    val descripcion: String? = null,
    val categoryId: String,
    val marca: String? = null,
    val attributes: JsonObject = JsonObject(emptyMap()),
    val imagenes: List<String> = emptyList(),
)

data class VariantDraft(
    val productId: String,
    val sku: String,
    val attributes: JsonObject = JsonObject(emptyMap()),
    val precioVenta: Double,
    val costo: Double = 0.0,
)

// ---------- Repositorio ----------

/**
 * Lecturas: Flow desde el caché Room (fuente única de lectura).
 * Escrituras: directo a Supabase (RLS decide: solo admin) y refresh del caché.
 */
interface CatalogRepository {

    fun observeCategories(includeInactive: Boolean = false): Flow<List<Category>>
    fun observeProducts(): Flow<List<ProductWithVariants>>

    /** Todos los productos y variantes, incluidos inactivos (administración). */
    fun observeAllProducts(): Flow<List<ProductWithVariants>>
    fun observeProductsByCategory(categoryId: String): Flow<List<ProductWithVariants>>
    fun observeProduct(productId: String): Flow<ProductWithVariants?>
    fun searchProducts(query: String): Flow<List<ProductWithVariants>>

    /** Busca en caché por SKU exacto (escáner del punto de venta). */
    suspend fun findVariantBySku(sku: String): ProductVariant?

    /** Descarga el catálogo completo de Supabase y reemplaza el caché. */
    suspend fun refreshCatalog(): DataResult<Unit>

    // -- Escrituras admin --
    suspend fun createCategory(draft: CategoryDraft): DataResult<Category>
    suspend fun updateCategory(category: Category): DataResult<Category>
    suspend fun createProduct(draft: ProductDraft): DataResult<Product>
    suspend fun updateProduct(product: Product): DataResult<Product>
    suspend fun createVariant(draft: VariantDraft): DataResult<ProductVariant>
    suspend fun updateVariant(variant: ProductVariant): DataResult<ProductVariant>
}
