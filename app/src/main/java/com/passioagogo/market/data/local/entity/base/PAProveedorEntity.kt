package com.passioagogo.market.data.local.entity.base

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "proveedores",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class ProveedorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val direccion: String?,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false
)
