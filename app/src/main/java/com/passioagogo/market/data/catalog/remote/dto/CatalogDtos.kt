package com.passioagogo.market.data.catalog.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * DTOs espejo de 01_catalog.sql. Los nombres @SerialName coinciden
 * columna a columna con la tabla en Supabase.
 *
 * Nota sobre dinero: numeric(10,2) se mapea a Double porque el cliente
 * solo captura y muestra precios — los totales, congelamiento de precio
 * y márgenes los calcula PostgreSQL (triggers de 08 y vistas de 11).
 */
@Serializable
data class CategoryDto(
    val id: String,
    val nombre: String,
    @SerialName("parent_id") val parentId: String? = null,
    val descripcion: String? = null,
    val activo: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProductDto(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    /** Texto corto para la tarjeta del catálogo web. */
    val resumen: String? = null,
    /** Se consigue por encargo aunque no esté activo en tienda. */
    @SerialName("sobre_pedido") val sobrePedido: Boolean = false,
    /** Texto corto para la tarjeta del catálogo web. */
    @SerialName("category_id") val categoryId: String,
    val marca: String? = null,
    val attributes: JsonObject = JsonObject(emptyMap()),
    val imagenes: List<String> = emptyList(),
    val activo: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProductVariantDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    val sku: String,
    val attributes: JsonObject = JsonObject(emptyMap()),
    @SerialName("precio_venta") val precioVenta: Double,
    val costo: Double = 0.0,
    val activo: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

// ---------- Payloads de escritura (sin id ni timestamps: los pone la BD) ----------

@Serializable
data class NewCategoryDto(
    val nombre: String,
    @SerialName("parent_id") val parentId: String? = null,
    val descripcion: String? = null,
)

@Serializable
data class NewProductDto(
    val nombre: String,
    val descripcion: String? = null,
    val resumen: String? = null,
    @SerialName("sobre_pedido") val sobrePedido: Boolean = false,
    @SerialName("category_id") val categoryId: String,
    val marca: String? = null,
    val attributes: JsonObject = JsonObject(emptyMap()),
    val imagenes: List<String> = emptyList(),
)

@Serializable
data class NewProductVariantDto(
    @SerialName("product_id") val productId: String,
    val sku: String,
    val attributes: JsonObject = JsonObject(emptyMap()),
    @SerialName("precio_venta") val precioVenta: Double,
    val costo: Double = 0.0,
)
