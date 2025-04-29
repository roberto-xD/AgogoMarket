package com.passioagogo.market.data.local.converters

import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.bean.PAProductBean

fun PAProductBean.toEntity(): PAProductEntity {
    return PAProductEntity(
        nombre = this.title.orEmpty(),
    )
}