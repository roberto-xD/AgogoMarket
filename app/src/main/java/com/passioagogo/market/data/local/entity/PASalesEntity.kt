package com.passioagogo.market.data.local.entity

import PADataConstants.SALES_TABLE
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = SALES_TABLE
)
data class PASalesEntity(
    @PrimaryKey(autoGenerate = true) val salesId : Int = 0,
    val ultima_edicion : String = "",
    val metodo_de_pago : String = "",
    val metodo_de_envio : String = "",
    val fecha_de_venta : String = "",
    val total : String = "",
    val estatus : String = "",
    val cliente : String = "",
)