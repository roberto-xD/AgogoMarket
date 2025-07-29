package com.passioagogo.market.data.local.entity.base

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class ProveedorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)
