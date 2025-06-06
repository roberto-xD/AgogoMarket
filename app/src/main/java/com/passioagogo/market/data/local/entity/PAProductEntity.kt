package com.passioagogo.market.data.local.entity
import PADataConstants.PRODUCT_TABLE
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = PRODUCT_TABLE
)
data class PAProductEntity(
    @PrimaryKey(autoGenerate = true) val productId : Int = 0,
    val nombre : String = "",
    val descripcion : String = "",
    val imagenes : String = "",
    val precio : String = "",
    val stock : String = "",
    val categoria : String = "",
)
