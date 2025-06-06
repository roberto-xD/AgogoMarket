package com.passioagogo.market.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import PADataConstants.PURCHASES_TABLE

@Entity(
    tableName = PURCHASES_TABLE
)
data class PAPurchasesEntity(
    @PrimaryKey(autoGenerate = true) val purchasesId : Int = 0,
    val fecha_de_compra : String = "",
    val cantidad_comprada : String = "",
    val total : String = "",
    val estatus : String = "",
    val proveedor : String = "",
)