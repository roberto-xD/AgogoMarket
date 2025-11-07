package com.passioagogo.market.data.local.entity.base

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subcategorias",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoriaId"]),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class SubcategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val nombre: String,
    val descripcion: String?,
    val categoriaId: Long,
    val categoriaRemoteId: String? = null,  // FK remota
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false
)
