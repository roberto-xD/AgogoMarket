package com.passioagogo.market.domain.bean

import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams
import com.passioagogo.market.ui.decorators.orZero

data class ProductoImportExtendido(
    val productoBase: ProductoImport,
    val familiasNombres: List<String> = emptyList(),
    val categoriasNombres: List<String> = emptyList(),
    val subcategoriasNombres: List<String> = emptyList(),
    val atributos: Map<String, String> = emptyMap()
) {
    fun toProductoImport(): ProductoImport {
        return productoBase
    }

    fun toCrearProductoParamsExtendido(
        familiaId: Long ?= null,
        categoriaId: Long ?= null,
        subcategoriaId: Long ?= null,
    ): GuardarProductoParams {
        return GuardarProductoParams(
            nombre = productoBase.nombre,
            descripcion = productoBase.descripcion.orEmpty(),
            skuInterno = productoBase.skuInterno,
            codigoBarras = productoBase.codigoBarras.orEmpty(),
            precioCompra = productoBase.precioCompra,
            precioVenta = productoBase.precioVenta,
            cantidadInicial = productoBase.cantidadActual,
            cantidadMinima = productoBase.cantidadMinima,
            proveedorPrincipalId = productoBase.proveedorPrincipalId.orZero(),
            familiaId = familiaId.orZero(),
            categoriaId = categoriaId.orZero(),
            subcategoriaId = subcategoriaId.orZero(),
            atributos = atributos
        )
    }
}