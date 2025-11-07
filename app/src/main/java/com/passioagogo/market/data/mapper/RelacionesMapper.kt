package com.passioagogo.market.data.mapper

import com.passioagogo.market.data.local.entity.relation.*
import com.passioagogo.market.data.remote.dto.*

// ProductoFamilia
fun ProductoFamiliaDto.toEntity(
    localProductoId: Long,
    localFamiliaId: Long
): ProductoFamiliaEntity {
    return ProductoFamiliaEntity(
        productoId = localProductoId,
        familiaId = localFamiliaId,
        productoRemoteId = productoId,
        familiaRemoteId = familiaId,
        isSynced = true,
        needsSync = false
    )
}

fun ProductoFamiliaEntity.toDto(): ProductoFamiliaDto {
    return ProductoFamiliaDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        familiaId = familiaRemoteId ?: throw IllegalStateException("familiaRemoteId no puede ser null")
    )
}

// ProductoCategoria
fun ProductoCategoriaDto.toEntity(
    localProductoId: Long,
    localCategoriaId: Long
): ProductoCategoriaEntity {
    return ProductoCategoriaEntity(
        productoId = localProductoId,
        categoriaId = localCategoriaId,
        productoRemoteId = productoId,
        categoriaRemoteId = categoriaId,
        esPrincipal = esPrincipal,
        isSynced = true,
        needsSync = false
    )
}

fun ProductoCategoriaEntity.toDto(): ProductoCategoriaDto {
    return ProductoCategoriaDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        categoriaId = categoriaRemoteId ?: throw IllegalStateException("categoriaRemoteId no puede ser null"),
        esPrincipal = esPrincipal
    )
}

// ProductoSubcategoria
fun ProductoSubcategoriaDto.toEntity(
    localProductoId: Long,
    localSubcategoriaId: Long
): ProductoSubcategoriaEntity {
    return ProductoSubcategoriaEntity(
        productoId = localProductoId,
        subcategoriaId = localSubcategoriaId,
        productoRemoteId = productoId,
        subcategoriaRemoteId = subcategoriaId,
        isSynced = true,
        needsSync = false
    )
}

fun ProductoSubcategoriaEntity.toDto(): ProductoSubcategoriaDto {
    return ProductoSubcategoriaDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        subcategoriaId = subcategoriaRemoteId ?: throw IllegalStateException("subcategoriaRemoteId no puede ser null")
    )
}

// ProductoProveedor
fun ProductoProveedorDto.toEntity(
    localProductoId: Long,
    localProveedorId: Long
): ProductoProveedorEntity {
    return ProductoProveedorEntity(
        productoId = localProductoId,
        proveedorId = localProveedorId,
        productoRemoteId = productoId,
        proveedorRemoteId = proveedorId,
        precioCompra = precioCompra,
        fechaUltimaCompra = DateMapper.parseIsoDate(fechaUltimaCompra),
        activo = activo,
        isSynced = true,
        needsSync = false
    )
}

fun ProductoProveedorEntity.toDto(): ProductoProveedorDto {
    return ProductoProveedorDto(
        productoId = productoRemoteId ?: throw IllegalStateException("productoRemoteId no puede ser null"),
        proveedorId = proveedorRemoteId ?: throw IllegalStateException("proveedorRemoteId no puede ser null"),
        precioCompra = precioCompra,
        fechaUltimaCompra = DateMapper.formatToIso(fechaUltimaCompra),
        activo = activo
    )
}