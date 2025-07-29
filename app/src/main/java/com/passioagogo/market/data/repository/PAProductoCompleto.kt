package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import com.passioagogo.market.data.local.entity.utils.ProductoAtributoConTipo
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity
import com.passioagogo.market.data.local.entity.utils.ProductoProveedorConNombre

data class ProductoCompleto(
    val producto: ProductoEntity,
    val categorias: List<CategoriaEntity> = emptyList(),
    val subcategorias: List<SubcategoriaEntity> = emptyList(),
    val proveedores: List<ProductoProveedorConNombre> = emptyList(),
    val atributos: List<ProductoAtributoConTipo> = emptyList(),
    val imagenes: List<ProductoImagenEntity> = emptyList()
)