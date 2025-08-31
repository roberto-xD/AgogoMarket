package com.passioagogo.market.domain.bean

data class VistaPreviaCompleta(
    val productos: List<ProductoImport>,
    val familias: List<FamiliaImport>,
    val categorias: List<CategoriaImport>,
    val subcategorias: List<SubcategoriaImport>,
    val totalProductos: Int,
    val totalFamilias: Int,
    val totalCategorias: Int,
    val totalSubcategorias: Int
)