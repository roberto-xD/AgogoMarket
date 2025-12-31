package com.passioagogo.market.data.local.entity.base

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "familias")
data class FamiliaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String, // juguetes_adultos, consumibles, lenceria
    val descripcion: String,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
)
