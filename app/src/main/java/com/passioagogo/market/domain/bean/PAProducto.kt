package com.passioagogo.market.domain.bean

import com.passioagogo.market.data.local.entity.base.ProductoEntity

data class Producto(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String?,
    val codigoBarras: String?,
    val skuInterno: String,
    val precioCompra: Double,
    val precioVenta: Double,
    val cantidadActual: Int = 0,
    val cantidadMinima: Int = 0,
    val cantidadMaximaComprada: Int = 0,
    val proveedorPrincipalId: Long?,
    val color: String?,
    val fechaUltimaVenta: Long? = null,
    val fechaUltimaCompra: Long? = null,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) {

    // Propiedades calculadas útiles para la lógica de negocio
    val margenGanancia: Double
        get() = if (precioCompra > 0) {
            ((precioVenta - precioCompra) / precioCompra) * 100
        } else 0.0

    val valorInventario: Double
        get() = cantidadActual * precioCompra

    val necesitaRestock: Boolean
        get() = cantidadActual <= cantidadMinima

    val tiempoSinVender: Long?
        get() = fechaUltimaVenta?.let { System.currentTimeMillis() - it }

    companion object {
        fun fromEntity(entity: ProductoEntity): Producto {
            return Producto(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                codigoBarras = entity.codigoBarras,
                skuInterno = entity.skuInterno,
                precioCompra = entity.precioCompra,
                precioVenta = entity.precioVenta,
                cantidadActual = entity.cantidadActual,
                cantidadMinima = entity.cantidadMinima,
                cantidadMaximaComprada = entity.cantidadMaximaComprada,
                proveedorPrincipalId = entity.proveedorPrincipalId,
                color = entity.color,
                fechaUltimaVenta = entity.fechaUltimaVenta,
                fechaUltimaCompra = entity.fechaUltimaCompra,
                activo = entity.activo,
                fechaCreacion = entity.fechaCreacion,
                fechaActualizacion = entity.fechaActualizacion
            )
        }
    }

    fun toEntity(): ProductoEntity {
        return ProductoEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            codigoBarras = codigoBarras,
            skuInterno = skuInterno,
            precioCompra = precioCompra,
            precioVenta = precioVenta,
            cantidadActual = cantidadActual,
            cantidadMinima = cantidadMinima,
            cantidadMaximaComprada = cantidadMaximaComprada,
            proveedorPrincipalId = proveedorPrincipalId,
            color = color,
            fechaUltimaVenta = fechaUltimaVenta,
            fechaUltimaCompra = fechaUltimaCompra,
            activo = activo,
            fechaCreacion = fechaCreacion,
            fechaActualizacion = fechaActualizacion
        )
    }
}