package com.passioagogo.market.domain.bean

import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.domain.model.venta.ProductoVenta
import com.passioagogo.market.ui.decorators.orZero

data class Producto(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String?= null,
    val imagenPrincipal: String = "",
    val codigoBarras: String?= null,
    val skuInterno: String = "",
    val precioCompra: Double = 0.0,
    val precioVenta: Double = 0.0,
    val cantidadActual: Int = 0,
    val cantidadMinima: Int = 0,
    val cantidadMaximaComprada: Int = 0,
    val proveedorPrincipalId: Long? = null,
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
        fun fromEntity(
            entity: ProductoEntity,
        ): Producto {
            return Producto(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                codigoBarras = entity.codigoBarras,
                skuInterno = entity.skuInterno.orEmpty(),
                precioCompra = entity.precioCompra.orZero(),
                precioVenta = entity.precioVenta.orZero(),
                cantidadActual = entity.cantidadActual.orZero(),
                cantidadMinima = entity.cantidadMinima.orZero(),
                cantidadMaximaComprada = entity.cantidadMaximaComprada.orZero(),
                proveedorPrincipalId = entity.proveedorPrincipalId,
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
            fechaUltimaVenta = fechaUltimaVenta,
            fechaUltimaCompra = fechaUltimaCompra,
            activo = activo,
            fechaCreacion = fechaCreacion,
            fechaActualizacion = fechaActualizacion
        )
    }

    fun toVenta(): ProductoVenta {
        return ProductoVenta(
            productoId = id,
            nombre = nombre,
            cantidad = 0,
            precioUnitario = precioVenta,
            stockDisponible = cantidadActual,
        )
    }
}