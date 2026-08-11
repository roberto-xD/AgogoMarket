package com.passioagogo.market.data.catalog

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.onSuccess
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.catalog.local.CatalogDao
import com.passioagogo.market.data.catalog.mapper.toDomain
import com.passioagogo.market.data.catalog.mapper.toEntity
import com.passioagogo.market.data.catalog.remote.dto.CategoryDto
import com.passioagogo.market.data.catalog.remote.dto.NewCategoryDto
import com.passioagogo.market.data.catalog.remote.dto.NewProductDto
import com.passioagogo.market.data.catalog.remote.dto.NewProductVariantDto
import com.passioagogo.market.data.catalog.remote.dto.ProductDto
import com.passioagogo.market.data.catalog.remote.dto.ProductVariantDto
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.catalog.Category
import com.passioagogo.market.domain.catalog.CategoryDraft
import com.passioagogo.market.domain.catalog.Product
import com.passioagogo.market.domain.catalog.ProductDraft
import com.passioagogo.market.domain.catalog.ProductVariant
import com.passioagogo.market.domain.catalog.ProductWithVariants
import com.passioagogo.market.domain.catalog.VariantDraft
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val dao: CatalogDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CatalogRepository {

    private companion object {
        const val CATEGORIES = "categories"
        const val PRODUCTS = "products"
        const val VARIANTS = "product_variants"
    }

    // ---------- Lecturas: Flow desde Room ----------

    override fun observeCategories(includeInactive: Boolean): Flow<List<Category>> {
        val source = if (includeInactive) dao.observeAllCategories() else dao.observeCategories()
        return source.map { list -> list.map { it.toDomain() } }
    }

    override fun observeProducts(): Flow<List<ProductWithVariants>> =
        dao.observeProducts().map { list -> list.map { it.toDomain() } }

    override fun observeAllProducts(): Flow<List<ProductWithVariants>> =
        dao.observeAllProducts().map { list ->
            list.map { it.toDomain(includeInactiveVariants = true) }
        }

    override fun observeProductsByCategory(categoryId: String): Flow<List<ProductWithVariants>> =
        dao.observeProductsByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun observeProduct(productId: String): Flow<ProductWithVariants?> =
        dao.observeProduct(productId).map { it?.toDomain() }

    override fun searchProducts(query: String): Flow<List<ProductWithVariants>> =
        dao.searchProducts(query).map { list -> list.map { it.toDomain() } }

    override suspend fun findVariantBySku(sku: String): ProductVariant? =
        withContext(io) { dao.findVariantBySku(sku)?.toDomain() }

    // ---------- Refresh: Supabase → Room ----------

    override suspend fun refreshCatalog(): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            // Las tres descargas en paralelo; RLS ya filtra según el rol
            coroutineScope {
                val categories = async {
                    postgrest.from(CATEGORIES).select().decodeList<CategoryDto>()
                }
                val products = async {
                    postgrest.from(PRODUCTS).select().decodeList<ProductDto>()
                }
                val variants = async {
                    postgrest.from(VARIANTS).select().decodeList<ProductVariantDto>()
                }
                dao.replaceAll(
                    categories = categories.await().map { it.toEntity() },
                    products = products.await().map { it.toEntity() },
                    variants = variants.await().map { it.toEntity() },
                )
            }
        }
    }

    // ---------- Escrituras admin: Supabase primero, luego caché ----------

    override suspend fun createCategory(draft: CategoryDraft): DataResult<Category> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(CATEGORIES).insert(
                    NewCategoryDto(draft.nombre, draft.parentId, draft.descripcion)
                ) { select() }.decodeSingle<CategoryDto>()
            }.onSuccess { dao.insertCategories(listOf(it.toEntity())) }
                .map { it.toDomain() }
        }

    override suspend fun updateCategory(category: Category): DataResult<Category> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(CATEGORIES).update({
                    set("nombre", category.nombre)
                    set("parent_id", category.parentId)
                    set("descripcion", category.descripcion)
                    set("activo", category.activo)
                }) {
                    select()
                    filter { eq("id", category.id) }
                }.decodeSingle<CategoryDto>()
            }.onSuccess { dao.insertCategories(listOf(it.toEntity())) }
                .map { it.toDomain() }
        }

    override suspend fun createProduct(draft: ProductDraft): DataResult<Product> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PRODUCTS).insert(
                    NewProductDto(
                        nombre = draft.nombre,
                        descripcion = draft.descripcion,
                        resumen = draft.resumen,
                        sobrePedido = draft.sobrePedido,
                        categoryId = draft.categoryId,
                        marca = draft.marca,
                        attributes = draft.attributes,
                        imagenes = draft.imagenes,
                    )
                ) { select() }.decodeSingle<ProductDto>()
            }.onSuccess { dao.insertProducts(listOf(it.toEntity())) }
                .map { it.toDomain() }
        }

    override suspend fun updateProduct(product: Product): DataResult<Product> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PRODUCTS).update({
                    set("nombre", product.nombre)
                    set("descripcion", product.descripcion)
                    set("resumen", product.resumen)
                    set("sobre_pedido", product.sobrePedido)
                    set("category_id", product.categoryId)
                    set("marca", product.marca)
                    set("attributes", product.attributes)
                    set("imagenes", product.imagenes)
                    set("activo", product.activo)
                }) {
                    select()
                    filter { eq("id", product.id) }
                }.decodeSingle<ProductDto>()
            }.onSuccess { dao.insertProducts(listOf(it.toEntity())) }
                .map { it.toDomain() }
        }

    override suspend fun createVariant(draft: VariantDraft): DataResult<ProductVariant> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(VARIANTS).insert(
                    NewProductVariantDto(
                        productId = draft.productId,
                        sku = draft.sku,
                        attributes = draft.attributes,
                        precioVenta = draft.precioVenta,
                        costo = draft.costo,
                    )
                ) { select() }.decodeSingle<ProductVariantDto>()
            }.onSuccess { dao.insertVariants(listOf(it.toEntity())) }
                .map { it.toDomain() }
        }

    override suspend fun updateVariant(variant: ProductVariant): DataResult<ProductVariant> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(VARIANTS).update({
                    set("sku", variant.sku)
                    set("attributes", variant.attributes)
                    set("precio_venta", variant.precioVenta)
                    set("costo", variant.costo)
                    set("activo", variant.activo)
                }) {
                    select()
                    filter { eq("id", variant.id) }
                }.decodeSingle<ProductVariantDto>()
            }.onSuccess { dao.insertVariants(listOf(it.toEntity())) }
                .map { it.toDomain() }
        }
}
