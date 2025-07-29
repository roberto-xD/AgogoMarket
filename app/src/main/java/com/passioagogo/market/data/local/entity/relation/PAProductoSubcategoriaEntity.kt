package com.passioagogo.market.data.local.entity.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity

@Entity(
    tableName = "producto_subcategorias",
    primaryKeys = ["productoId", "subcategoriaId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubcategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["subcategoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productoId"]),
        Index(value = ["subcategoriaId"])
    ]
)
data class ProductoSubcategoriaEntity(
    val productoId: Long,
    val subcategoriaId: Long
)
