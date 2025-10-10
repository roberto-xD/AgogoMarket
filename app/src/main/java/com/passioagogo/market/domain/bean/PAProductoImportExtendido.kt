package com.passioagogo.market.domain.bean

import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams

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
        categoriaIds: List<Long> = emptyList(),
        subcategoriaIds: List<Long> = emptyList()
    ): GuardarProductoParams {
        return GuardarProductoParams(
            nombre = productoBase.nombre,
            descripcion = productoBase.descripcion,
            skuInterno = productoBase.skuInterno,
            codigoBarras = productoBase.codigoBarras,
            precioCompra = productoBase.precioCompra,
            precioVenta = productoBase.precioVenta,
            cantidadInicial = productoBase.cantidadActual,
            cantidadMinima = productoBase.cantidadMinima,
            proveedorPrincipalId = productoBase.proveedorPrincipalId,
            color = productoBase.color,
            familiaId = familiaId,
            categorias = categoriaIds,
            subcategorias = subcategoriaIds,
            atributos = atributos
        )
    }
}