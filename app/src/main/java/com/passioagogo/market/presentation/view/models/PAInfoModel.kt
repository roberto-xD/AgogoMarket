package com.passioagogo.market.presentation.view.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

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
    val createDateBuy   : MutableState<Long> = mutableStateOf(0L),
    val lastDateSell    : MutableState<Long> = mutableStateOf(0L),
    val active          : MutableState<Boolean> = mutableStateOf(false),
    val familyList      : MutableList<String> = mutableListOf(),
    val categoryList    : MutableList<String> = mutableListOf(),
    val subcategoryList : MutableList<String> = mutableListOf(),
    )