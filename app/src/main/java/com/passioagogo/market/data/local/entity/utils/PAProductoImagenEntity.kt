package com.passioagogo.market.data.local.entity.utils

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.passioagogo.market.data.local.entity.base.ProductoEntity

@Entity(
    tableName = "producto_imagenes",
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
        Index(value = ["remoteId"], unique = true)
    ]
)
data class ProductoImagenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val productoId: Long,
    val productoRemoteId: String? = null,  // Para sincronización
    val rutaImagen: String, // Ruta local del archivo
    val orden: Int = 0, // Para ordenar las imágenes
    val esPrincipal: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    // Campos de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false
)
