package com.passioagogo.market.presentation.view.models

import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams

data class PAInfoModel(
    val id: Long ?= null,
    val tittle          : String = "",
    val description     : String = "",
    val pathImageList   : List<PAImageModel> = listOf(),
    val sku             : String = "",
    val codigoBarra     : String = "",
    val currentStock    : String = "",
    val minStock        : String = "",
    val firtsStock      : String = "",
    val buyPrice        : String = "",
    val sellPrice       : String = "",
    val familyId        : Long = 0L,
    val category        : String = "",
    val subcategory     : String = "",
    val provider        : String = "",
    val createDateBuy   : Long? = 0L,
    val lastDateSell    : Long? = 0L,
    val active          : Boolean = false,
    val familyList      : List<PAFamiliasModel> = listOf(),
    val categoryList    : List<String> = listOf(),
    val subcategoryList : List<String> = listOf(),
    ){
        fun isEmpty(): Boolean{
            return tittle.isEmpty()
        }

        fun update(
            data : ProductoDetallado
        ): PAInfoModel {
            return this.copy(
                id = data.producto.id,
                tittle = data.producto.nombre,
                description = data.producto.descripcion.orEmpty(),
                pathImageList = data.imagenes.map {
                    it.toImageProductModel()
                },
                familyId = data.familia,
                sku = data.producto.skuInterno,
                codigoBarra = data.producto.codigoBarras.orEmpty(),
                currentStock = data.producto.cantidadActual.toString(),
                minStock = data.producto.cantidadMinima.toString(),
                buyPrice = data.producto.cantidadMinima.toString(),
                sellPrice = data.producto.precioVenta.toString(),
                provider = data.proveedores.toString(),
                createDateBuy = data.producto.fechaUltimaCompra,
                lastDateSell = data.producto.fechaUltimaVenta,
                active =data.producto.activo,
            )
        }

        fun toActualizaProductoParams(): GuardarProductoParams {
            return GuardarProductoParams(
                id = id ?: 0L,
                nombre = tittle,
                descripcion = description,
                skuInterno = sku,
                codigoBarras = codigoBarra,
                precioCompra = buyPrice.toDouble(),
                precioVenta = sellPrice.toDouble(),
                cantidadMinima = minStock.toInt(),
                cantidadActual = currentStock.toInt(),
                imagenes = pathImageList.map { it },
//                cantidadInicial = if(id == 0L) currentStock.toInt() else firtsStock.toInt(), todo corregir esto
//                categorias = emptyList(),
//                subcategorias = emptyList(),
            )
        }
    }