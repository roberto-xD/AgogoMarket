package com.passioagogo.market.data.local.entity.venta

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.passioagogo.market.data.local.entity.base.ProductoEntity

@Entity(
    tableName = "venta_productos",
    foreignKeys = [
        ForeignKey(
            entity = VentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("ventaId"), Index("productoId")]
)
data class VentaProductoEntity(
    @PrimaryKey()
    val id: Long = 0,
    val ventaId: Long,
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)