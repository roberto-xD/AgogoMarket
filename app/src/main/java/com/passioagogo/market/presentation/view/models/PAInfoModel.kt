package com.passioagogo.market.presentation.view.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.passioagogo.market.domain.bean.ProductoDetallado

data class PAInfoModel(
    val id: Long = 0,
    val tittle          : MutableState<String> = mutableStateOf(""),
    val description     : MutableState<String> = mutableStateOf(""),
    val pathImageList   : MutableList<String> = mutableListOf(),
    val sku             : MutableState<String> = mutableStateOf(""),
    val codigoBarra     : MutableState<String> = mutableStateOf(""),
    val currentStock    : MutableState<String> = mutableStateOf(""),
    val minStock        : MutableState<String> = mutableStateOf(""),
    val buyPrice        : MutableState<String> = mutableStateOf(""),
    val sellPrice       : MutableState<String> = mutableStateOf(""),
    val family          : MutableState<String> = mutableStateOf(""),
    val category        : MutableState<String> = mutableStateOf(""),
    val subcategory     : MutableState<String> = mutableStateOf(""),
    val provider        : MutableState<String> = mutableStateOf(""),
    val createDateBuy   : MutableState<Long?> = mutableStateOf(0L),
    val lastDateSell    : MutableState<Long?> = mutableStateOf(0L),
    val active          : MutableState<Boolean> = mutableStateOf(false),
    val familyList      : MutableList<String> = mutableListOf(),
    val categoryList    : MutableList<String> = mutableListOf(),
    val subcategoryList : MutableList<String> = mutableListOf(),
    ){
        fun isEmpty(): Boolean{
            return tittle.value.isEmpty()
        }

        fun update(
            data : ProductoDetallado
        ): PAInfoModel {
            return this.copy(
                id = data.producto.id,
                tittle = mutableStateOf(data.producto.nombre),
                description = mutableStateOf(data.producto.descripcion.orEmpty()),
                pathImageList = data.imagenes.map { it.rutaImagen }.toMutableList(),
                sku = mutableStateOf(data.producto.skuInterno),
                codigoBarra = mutableStateOf(data.producto.codigoBarras.orEmpty()),
                currentStock = mutableStateOf(data.producto.cantidadActual.toString()),
                minStock = mutableStateOf(data.producto.cantidadMinima.toString()),
                buyPrice = mutableStateOf(data.producto.cantidadMinima.toString()),
                sellPrice = mutableStateOf(data.producto.precioVenta.toString()),
                provider = mutableStateOf(data.proveedores.toString()),
                createDateBuy = mutableStateOf(data.producto.fechaUltimaCompra),
                lastDateSell = mutableStateOf(data.producto.fechaUltimaVenta),
                active = mutableStateOf(data.producto.activo),
            )
        }
    }