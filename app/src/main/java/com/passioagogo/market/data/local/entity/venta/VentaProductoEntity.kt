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
    indices = [
        Index("ventaId"),
        Index("productoId"),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class VentaProductoEntity(
    @PrimaryKey()
    val id: Long = 0,
    val remoteId: String? = null,
    val ventaId: Long,
    val productoId: Long,
    val ventaRemoteId: String? = null,  // Para sincronización
    val productoRemoteId: String? = null,  // Para sincronización
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,

    // Campos de sincronización
    val isSynced: Boolean = false,
    val needsSync: Boolean = false
)