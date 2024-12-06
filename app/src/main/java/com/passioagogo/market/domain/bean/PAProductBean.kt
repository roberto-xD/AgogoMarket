package com.passioagogo.market.domain.bean

data class PAProductBean(
    val Id : String,
    val Name : String,
    val Description: String ?= null,
    val Images: List<String> ?= emptyList(),
    val PriceBase: String,
    val Category: String,
    val PriceWithDiscount: String ?= null,
    val FlagDiscount: Boolean = false,
    val FlagAvailability: String ?= null,
    )
