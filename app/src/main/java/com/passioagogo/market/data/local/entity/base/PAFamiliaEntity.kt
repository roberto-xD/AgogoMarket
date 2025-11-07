package com.passioagogo.market.data.local.entity.base

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "familias")
data class FamiliaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,  // UUID de Supabase
    val userId: String? = null,  // NUEVO: Para RLS
    val nombre: String, // juguetes_adultos, consumibles, lenceria
    val descripcion: String,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    // Campos de control de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false
)
