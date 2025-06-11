package com.passioagogo.market.data.local.entity

import PADataConstants.PRICE_TABLE
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = PRICE_TABLE
)
data class PAPriceEntity(
    @PrimaryKey() val productId : Int = 0,
    val precio_publico : String = "",
    val costo : String = "",
    val precio_descuento : String = "",
    val costo_envio : String = "",
)