package com.passioagogo.market.domain.bean

import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams

data class ProductoDetallado(
    val producto: Producto = Producto(),
    val idFamilia: Long = 0L,
    val idCategoria: Long = 0L,
    val idSubCategoria: Long = 0L,
    val proveedores: List<ProveedorConPrecio> = emptyList(),
    val atributos: List<AtributoProducto> = emptyList(),
    val imagenes: List<ImagenProducto> = emptyList(),
    val historialPrecios: List<HistorialPrecio> = emptyList()
) {
    val imagenPrincipal: ImagenProducto?
        get() = imagenes.find { it.esPrincipal } ?: imagenes.firstOrNull()

    val proveedorMasBarato: ProveedorConPrecio?
        get() = proveedores.minByOrNull { it.precioCompra }

    val ultimoCambioPrecio: HistorialPrecio?
        get() = historialPrecios.maxByOrNull { it.fechaCambio }

    fun toActualizaProductoParams(): GuardarProductoParams {
        return GuardarProductoParams(
            id = producto.id,
            nombre = producto.nombre,
            descripcion = producto.descripcion.orEmpty(),
            skuInterno = producto.skuInterno,
            codigoBarras = producto.codigoBarras.orEmpty(),
            precioCompra = producto.precioCompra,
            precioVenta = producto.precioVenta,
            cantidadMinima = producto.cantidadMinima,
            cantidadActual = producto.cantidadActual,
            imagenes = imagenes,
        )
    }

    fun validateRules(): Boolean {
        return producto.nombre.isNotEmpty() && producto.cantidadActual != 0 && producto.precioVenta != 0.0
    }
}