package com.passioagogo.market.domain.bean

data class ProductoDetallado(
    val producto: Producto = Producto(),
    val familia: Long ?= 0L,
    val categorias: List<Long> = emptyList(),
    val subcategorias: List<Long> = emptyList(),
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


}