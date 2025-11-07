package com.passioagogo.market.data.local.entity.dinamics

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tipos_atributos",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class TipoAtributoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val nombre: String, // marca, material, base, sabor, recargable, etc.
    val tipoDato: String = "TEXT", // TEXT, NUMBER, BOOLEAN, DATE
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),

    // Campos de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false
)
