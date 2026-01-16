package com.passioagogo.market.data.local.entity.utils

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.passioagogo.market.data.local.entity.base.ProductoEntity

@Entity(
    tableName = "historial_precios",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productoId"]),
    ]
)
data class HistorialPrecioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val productoId: Long,
    val productoRemoteId: String? = null,  // Para sincronización
    val precioCompraAnterior: Double,
    val precioVentaAnterior: Double,
    val precioCompraNuevo: Double,
    val precioVentaNuevo: Double,
    val motivo: String?, // "Actualización manual", "Cambio de proveedor", etc.
    val fechaCambio: Long = System.currentTimeMillis(),
)
