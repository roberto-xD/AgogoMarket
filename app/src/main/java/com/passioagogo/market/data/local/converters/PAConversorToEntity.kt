package com.passioagogo.market.data.local.converters

import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.domain.bean.PAProductBean

fun PAProductBean.toEntity(): PAProductEntity {
    return PAProductEntity(
        sku = this.sku.orEmpty(),
        nombre = this.title.orEmpty(),
        descripcion = this.description.orEmpty(),
        imagenes = this.image.orEmpty(),
    )
}