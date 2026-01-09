package com.passioagogo.market.data.local.entity.base

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias",
    foreignKeys = [
        ForeignKey(
            entity = FamiliaEntity::class,
            parentColumns = ["id"],
            childColumns = ["familiaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["familiaId"])]
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String, // BDSM, Lubricantes, etc.
    val descripcion: String?,
    val familiaId: Long,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
)
