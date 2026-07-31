package com.passioagogo.market.data.catalog.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    // ---------- Lecturas (Flow: la UI reacciona a cada refresh) ----------

    @Query("SELECT * FROM categories WHERE activo = 1 ORDER BY nombre")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY nombre")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Transaction
    @Query("SELECT * FROM products WHERE activo = 1 ORDER BY nombre")
    fun observeProducts(): Flow<List<ProductWithVariantsLocal>>

    @Transaction
    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND activo = 1 ORDER BY nombre")
    fun observeProductsByCategory(categoryId: String): Flow<List<ProductWithVariantsLocal>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT p.* FROM products p
        LEFT JOIN product_variants v ON v.productId = p.id
        WHERE p.activo = 1 AND (
            p.nombre LIKE '%' || :query || '%'
            OR p.marca LIKE '%' || :query || '%'
            OR v.sku LIKE '%' || :query || '%'
        )
        ORDER BY p.nombre
        """
    )
    fun searchProducts(query: String): Flow<List<ProductWithVariantsLocal>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    fun observeProduct(productId: String): Flow<ProductWithVariantsLocal?>

    @Query("SELECT * FROM product_variants WHERE sku = :sku LIMIT 1")
    suspend fun findVariantBySku(sku: String): ProductVariantEntity?

    // ---------- Escrituras (solo las usa el refresh remoto) ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(items: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(items: List<ProductVariantEntity>)

    @Query("DELETE FROM categories") suspend fun clearCategories()
    @Query("DELETE FROM products") suspend fun clearProducts()
    @Query("DELETE FROM product_variants") suspend fun clearVariants()

    /**
     * Reemplazo atómico del caché completo: la UI nunca observa un
     * estado intermedio (p. ej. productos sin sus variantes).
     */
    @Transaction
    suspend fun replaceAll(
        categories: List<CategoryEntity>,
        products: List<ProductEntity>,
        variants: List<ProductVariantEntity>,
    ) {
        clearVariants()
        clearProducts()
        clearCategories()
        insertCategories(categories)
        insertProducts(products)
        insertVariants(variants)
    }
}
