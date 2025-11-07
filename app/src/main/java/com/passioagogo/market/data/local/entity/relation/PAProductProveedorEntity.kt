package com.passioagogo.market.data.local.entity.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.base.ProveedorEntity

@Entity(
    tableName = "producto_proveedores",
    primaryKeys = ["productoId", "proveedorId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProveedorEntity::class,
            parentColumns = ["id"],
            childColumns = ["proveedorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productoId"]),
        Index(value = ["proveedorId"])
    ]
)
data class ProductoProveedorEntity(
    val productoId: Long,
    val proveedorId: Long,
    val productoRemoteId: String? = null,  // Para sincronización
    val proveedorRemoteId: String? = null,  // Para sincronización
    val precioCompra: Double,
    val fechaUltimaCompra: Long?,
    val activo: Boolean = true,

    // Campos de sincronización
    val isSynced: Boolean = false,
    val needsSync: Boolean = false
)
