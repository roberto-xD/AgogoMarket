package com.passioagogo.market.data.local.entity.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import com.passioagogo.market.data.local.entity.base.ProductoEntity

@Entity(
    tableName = "producto_categorias",
    primaryKeys = ["productoId", "categoriaId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productoId"]),
        Index(value = ["categoriaId"])
    ]
)
data class ProductoCategoriaEntity(
    val productoId: Long,
    val categoriaId: Long,
    val productoRemoteId: String? = null,  // Para sincronización
    val categoriaRemoteId: String? = null,  // Para sincronización
    val esPrincipal: Boolean = false, // Para identificar la categoría principal
    val isSynced: Boolean = false,
    val needsSync: Boolean = false,
)
