package com.passioagogo.market.data.local.entity.base

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity

@Entity(
    tableName = "productos",
    foreignKeys = [
        ForeignKey(
            entity = ProveedorEntity::class,
            parentColumns = ["id"],
            childColumns = ["proveedorPrincipalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["codigoBarras"], unique = true),
        Index(value = ["skuInterno"]),
        Index(value = ["proveedorPrincipalId"])
    ]
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String?,
    val codigoBarras: String?,
    val skuInterno: String?,
    val precioCompra: Double ?= 0.0,
    val precioVenta: Double ?= 0.0,
    val cantidadActual: Int ?= 0,
    val cantidadMinima: Int ?= 0,
    val cantidadMaximaComprada: Int ?= 0,
    val proveedorPrincipalId: Long?,
    val fechaUltimaVenta: Long?,
    val fechaUltimaCompra: Long?,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)

data class ProductoConImagenes(
    @Embedded val producto: ProductoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productoId"
    )
    val imagenes: List<ProductoImagenEntity>
)