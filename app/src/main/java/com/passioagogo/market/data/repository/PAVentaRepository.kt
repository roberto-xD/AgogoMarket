package com.passioagogo.market.data.repository

import com.passioagogo.market.domain.model.venta.ProductoVenta
import com.passioagogo.market.domain.model.venta.Venta

interface VentaRepository {
    suspend fun guardarVenta(venta: Venta): Result<Long>
    suspend fun actualizarVenta(venta: Venta): Result<Unit>
    suspend fun obtenerVenta(id: Long): Result<Venta>
    suspend fun obtenerTodasVentas(): Result<List<Venta>>
    suspend fun eliminarVenta(id: Long): Result<Unit>
    suspend fun validarStock(productos: List<ProductoVenta>): Result<Unit>
}