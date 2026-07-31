package com.passioagogo.market.data.catalog.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Caché local del catálogo. Room es la única fuente de LECTURA de la app
 * (los repos exponen Flow desde aquí); las escrituras van a Supabase y
 * luego se refresca el caché.
 *
 * `attributes` e `imagenes` se guardan como JSON crudo (String) — el caché
 * no los indexa, solo los transporta.
 */
@Entity(tableName = "categories", indices = [Index("parentId")])
data class CategoryEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val parentId: String?,
    val descripcion: String?,
    val activo: Boolean,
    val updatedAt: String?,
)

@Entity(tableName = "products", indices = [Index("categoryId")])
data class ProductEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String?,
    val categoryId: String,
    val marca: String?,
    val attributesJson: String,
    val imagenesJson: String,
    val activo: Boolean,
    val updatedAt: String?,
)

@Entity(tableName = "product_variants", indices = [Index("productId"), Index("sku", unique = true)])
data class ProductVariantEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val sku: String,
    val attributesJson: String,
    val precioVenta: Double,
    val costo: Double,
    val activo: Boolean,
    val updatedAt: String?,
)

/** Producto con sus variantes (relación 1:N resuelta por Room). */
data class ProductWithVariantsLocal(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "id", entityColumn = "productId")
    val variants: List<ProductVariantEntity>,
)
