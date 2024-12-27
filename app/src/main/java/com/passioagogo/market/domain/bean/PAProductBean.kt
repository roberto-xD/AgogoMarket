package com.passioagogo.market.domain.bean

data class PAProductBean(
    val code: String = "",
    val tittle: String = "",
    val store: String = "",
    val description: String = "",
    val image: String = "",
    val price: PAPriceBean ?= null,
    val category: String = "",
    val link: String = "",
    val hasOffer: Boolean = false,
    val isActive: Boolean = false,
    )

data class PAPriceBean(
    val price_normal: Double = 0.0,
    val price_normal_og: String = "",
    val price_og: String = "",
)