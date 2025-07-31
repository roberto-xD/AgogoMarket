package com.passioagogo.market.domain.bean

data class ProductoDetallado(
    val producto: Producto,
    val categorias: List<Categoria> = emptyList(),
    val subcategorias: List<Subcategoria> = emptyList(),
    val proveedores: List<ProveedorConPrecio> = emptyList(),
    val atributos: List<AtributoProducto> = emptyList(),
    val imagenes: List<ImagenProducto> = emptyList(),
    val historialPrecios: List<HistorialPrecio> = emptyList()
) {
    val categoriaPrincipal: Categoria?
        get() = categorias.firstOrNull() // Simplificado, se puede mejorar con lógica específica

    val imagenPrincipal: ImagenProducto?
        get() = imagenes.find { it.esPrincipal } ?: imagenes.firstOrNull()

    val proveedorMasBarato: ProveedorConPrecio?
        get() = proveedores.minByOrNull { it.precioCompra }

    val ultimoCamboPrecio: HistorialPrecio?
        get() = historialPrecios.maxByOrNull { it.fechaCambio }
}