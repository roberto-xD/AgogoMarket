package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.model.catalogo.CatalogoCompleto
import com.passioagogo.market.domain.model.catalogo.ProductoCatalogo

interface CatalogoRepository {
    suspend fun getCatalogoCompleto(): CatalogoCompleto
    suspend fun getProductoParaCatalogo(productoId: Long): ProductoCatalogo?
    suspend fun getProductosPorCategoria(categoriaId: Long): List<ProductoCatalogo>
}