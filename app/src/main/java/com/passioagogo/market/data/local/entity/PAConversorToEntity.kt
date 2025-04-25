package com.passioagogo.market.data.local.entity

import com.passioagogo.market.domain.bean.PAProductBean

fun PAProductBean.toEntity():PAProductEntity {
    return PAProductEntity(
        nombre = this.title.orEmpty(),
    )
}