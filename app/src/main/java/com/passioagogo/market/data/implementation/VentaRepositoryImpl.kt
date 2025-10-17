package com.passioagogo.market.data.implementation

import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.VentaDao
import com.passioagogo.market.data.local.entity.venta.toDomain
import com.passioagogo.market.data.repository.VentaRepository
import com.passioagogo.market.domain.model.venta.ProductoVenta
import com.passioagogo.market.domain.model.venta.Venta
import com.passioagogo.market.domain.model.venta.toEntity
import com.passioagogo.market.ui.decorators.orZero
import javax.inject.Inject

class VentaRepositoryImpl @Inject constructor(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao
) : VentaRepository {

    override suspend fun guardarVenta(venta: Venta): Result<Long> = runCatching {
        validarStock(venta.productos).getOrThrow()

        val ventaEntity = venta.toEntity()
        val productosEntity = venta.productos.map { it.toEntity(0) }

        ventaDao.guardarVentaCompleta(ventaEntity, productosEntity)
    }

    override suspend fun actualizarVenta(venta: Venta): Result<Unit> = runCatching {
        validarStock(venta.productos).getOrThrow()

        val ventaEntity = venta.toEntity()
        val productosEntity = venta.productos.map { it.toEntity(venta.id) }

        ventaDao.actualizarVentaCompleta(ventaEntity, productosEntity)
    }

    override suspend fun obtenerVenta(id: Long): Result<Venta> = runCatching {
        val ventaConProductos = ventaDao.obtenerVentaConProductos(id)
            ?: throw Exception("Venta no encontrada")

        val productos = ventaConProductos.productos.map { vp ->
            ProductoVenta(
                productoId = vp.producto.id,
                nombre = vp.producto.nombre,
                cantidad = vp.ventaProducto.cantidad,
                precioUnitario = vp.ventaProducto.precioUnitario,
                stockDisponible = vp.producto.cantidadActual.orZero()
            )
        }

        ventaConProductos.venta.toDomain(productos)
    }

    override suspend fun obtenerTodasVentas(): Result<List<Venta>> = runCatching {
        ventaDao.obtenerTodasVentasConProductos().map { ventaConProductos ->
            val productos = ventaConProductos.productos.map { vp ->
                ProductoVenta(
                    productoId = vp.producto.id,
                    nombre = vp.producto.nombre,
                    cantidad = vp.ventaProducto.cantidad,
                    precioUnitario = vp.ventaProducto.precioUnitario,
                    stockDisponible = vp.producto.cantidadActual.orZero()
                )
            }
            ventaConProductos.venta.toDomain(productos)
        }
    }

    override suspend fun eliminarVenta(id: Long): Result<Unit> = runCatching {
        val venta = ventaDao.obtenerVentaConProductos(id)?.venta
            ?: throw Exception("Venta no encontrada")
        ventaDao.eliminarVenta(venta)
    }

    override suspend fun validarStock(productos: List<ProductoVenta>): Result<Unit> = runCatching {
        productos.forEach { productoVenta ->
            val producto = productoDao.obtenerProductoPorId(productoVenta.productoId)
                ?: throw Exception("Producto no encontrado: ${productoVenta.nombre}")

            if (producto.cantidadActual.orZero() < productoVenta.cantidad) {
                throw Exception(
                    "Stock insuficiente para ${productoVenta.nombre}. " +
                            "Disponible: ${producto.cantidadActual}, Solicitado: ${productoVenta.cantidad}"
                )
            }
        }
    }
}