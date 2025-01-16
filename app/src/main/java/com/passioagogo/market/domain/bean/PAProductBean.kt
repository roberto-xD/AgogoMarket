package com.passioagogo.market.domain.bean

data class PAProductBean(
    var id: String = "",
    val code: String = "",
    val title: String = "",
    val store: String = "",
    val description: String = "",
    val image: String = "",
    val price: PAPriceBean = PAPriceBean(),
    val category: String = "",
    val stock: String = "",
    val link: String = "",
    val hasOffer: Boolean = false,
    val isActive: Boolean = false,
    )

data class PAPriceBean(
    val price_normal: Double = 0.0,
    val price_normal_og: String = "",
    val price_og: String = "",
)