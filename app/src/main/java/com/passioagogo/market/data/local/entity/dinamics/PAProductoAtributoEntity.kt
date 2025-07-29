package com.passioagogo.market.data.local.entity.dinamics

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.passioagogo.market.data.local.entity.base.ProductoEntity

@Entity(
    tableName = "producto_atributos",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoAtributoEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipoAtributoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productoId"]),
        Index(value = ["tipoAtributoId"])
    ]
)
data class ProductoAtributoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productoId: Long,
    val tipoAtributoId: Long,
    val valor: String // Siempre como String, se convierte según tipoDato
)
