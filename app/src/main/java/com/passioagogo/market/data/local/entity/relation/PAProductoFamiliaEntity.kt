package com.passioagogo.market.data.local.entity.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import com.passioagogo.market.data.local.entity.base.ProductoEntity

@Entity(
    tableName = "producto_familia",
    primaryKeys = ["productoId","familiaId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FamiliaEntity::class,
            parentColumns = ["id"],
            childColumns = ["familiaId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["productoId"]),
        Index(value = ["familiaId"])
    ]
)
data class ProductoFamiliaEntity(
    val productoId: Long,
    val familiaId: Long
)