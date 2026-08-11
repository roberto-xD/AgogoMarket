package com.passioagogo.market.data.catalog.mapper

import com.passioagogo.market.data.catalog.local.CategoryEntity
import com.passioagogo.market.data.catalog.local.ProductEntity
import com.passioagogo.market.data.catalog.local.ProductVariantEntity
import com.passioagogo.market.data.catalog.local.ProductWithVariantsLocal
import com.passioagogo.market.data.catalog.remote.dto.CategoryDto
import com.passioagogo.market.data.catalog.remote.dto.ProductDto
import com.passioagogo.market.data.catalog.remote.dto.ProductVariantDto
import com.passioagogo.market.domain.catalog.Category
import com.passioagogo.market.domain.catalog.Product
import com.passioagogo.market.domain.catalog.ProductVariant
import com.passioagogo.market.domain.catalog.ProductWithVariants
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val json = Json { ignoreUnknownKeys = true }

private fun String.toJsonObject(): JsonObject =
    runCatching { json.decodeFromString(JsonObject.serializer(), this) }
        .getOrDefault(JsonObject(emptyMap()))

private fun String.toStringList(): List<String> =
    runCatching { json.decodeFromString(ListSerializer(String.serializer()), this) }
        .getOrDefault(emptyList())

// ---------- DTO → Entity (refresh remoto) ----------

fun CategoryDto.toEntity() = CategoryEntity(
    id = id, nombre = nombre, parentId = parentId,
    descripcion = descripcion, activo = activo, updatedAt = updatedAt,
)

fun ProductDto.toEntity() = ProductEntity(
    id = id, nombre = nombre, descripcion = descripcion,
    resumen = resumen, sobrePedido = sobrePedido,
    categoryId = categoryId, marca = marca,
    attributesJson = attributes.toString(),
    imagenesJson = json.encodeToString(ListSerializer(String.serializer()), imagenes),
    activo = activo, updatedAt = updatedAt,
)

fun ProductVariantDto.toEntity() = ProductVariantEntity(
    id = id, productId = productId, sku = sku,
    attributesJson = attributes.toString(),
    precioVenta = precioVenta, costo = costo,
    activo = activo, updatedAt = updatedAt,
)

// ---------- Entity → Dominio (lecturas) ----------

fun CategoryEntity.toDomain() = Category(
    id = id, nombre = nombre, parentId = parentId,
    descripcion = descripcion, activo = activo,
)

fun ProductEntity.toDomain() = Product(
    id = id, nombre = nombre, descripcion = descripcion,
    resumen = resumen, sobrePedido = sobrePedido,
    categoryId = categoryId, marca = marca,
    attributes = attributesJson.toJsonObject(),
    imagenes = imagenesJson.toStringList(),
    activo = activo,
)

fun ProductVariantEntity.toDomain() = ProductVariant(
    id = id, productId = productId, sku = sku,
    attributes = attributesJson.toJsonObject(),
    precioVenta = precioVenta, costo = costo, activo = activo,
)

fun ProductWithVariantsLocal.toDomain(
    includeInactiveVariants: Boolean = false,
) = ProductWithVariants(
    product = product.toDomain(),
    variants = variants
        .filter { includeInactiveVariants || it.activo }
        .map { it.toDomain() },
)

// ---------- DTO → Dominio (respuestas de escritura) ----------

fun CategoryDto.toDomain() = toEntity().toDomain()
fun ProductDto.toDomain() = toEntity().toDomain()
fun ProductVariantDto.toDomain() = toEntity().toDomain()
