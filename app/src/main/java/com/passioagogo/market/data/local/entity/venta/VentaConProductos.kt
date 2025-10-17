package com.passioagogo.market.data.local.entity.venta

import androidx.room.Embedded
import androidx.room.Relation
import com.passioagogo.market.data.local.entity.base.ProductoEntity

data class VentaConProductos(
    @Embedded val venta: VentaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ventaId",
        entity = VentaProductoEntity::class
    )
    val productos: List<VentaProductoConDetalles>
)

data class VentaProductoConDetalles(
    @Embedded val ventaProducto: VentaProductoEntity,
    @Relation(
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val producto: ProductoEntity
)