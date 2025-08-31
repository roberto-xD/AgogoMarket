package com.passioagogo.market.domain.bean

data class DatosImportCompletos(
    val productos: List<ProductoImport>,
    val familias: List<FamiliaImport>,
    val categorias: List<CategoriaImport>,
    val subcategorias: List<SubcategoriaImport>,
    val productosExtendidos: List<ProductoImportExtendido>
)