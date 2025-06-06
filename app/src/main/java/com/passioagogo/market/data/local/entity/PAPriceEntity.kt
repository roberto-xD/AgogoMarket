package com.passioagogo.market.data.local.entity

import PADataConstants.PRICE_TABLE
import androidx.room.Entity

@Entity(
    tableName = PRICE_TABLE
)
data class PAPriceEntity(
    val precio_publico : String = "",
    val costo : String = "",
    val precio_descuento : String = "",
    val costo_envio : String = "",
)