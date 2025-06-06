package com.passioagogo.market.data.local.entity

import PADataConstants.CATEGORY_TABLE
import androidx.room.Entity

@Entity(
    tableName = CATEGORY_TABLE
)
data class PACategoryEntity(
    val nombre : String = "",
    val descripcion : String = "",
)