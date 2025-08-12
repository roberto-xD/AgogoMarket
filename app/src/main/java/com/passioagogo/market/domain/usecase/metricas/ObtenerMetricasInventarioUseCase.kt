package com.passioagogo.market.domain.usecase.metricas

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class MetricasInventario(
    val totalProductos: Int,
    val valorTotalInventario: Double,
    val productosStockBajo: Int,
    val productosSinMovimiento: Int,
    val margenPromedioGanancia: Double,
    val topProductosVendidos: List<Producto>,
    val productosConMayorRotacion: List<Producto>
)

@Singleton
class ObtenerMetricasInventarioUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : NoParamsUseCase<MetricasInventario>() {

    override suspend fun execute(): MetricasInventario {
        val productos = productoRepository.obtenerTodosLosProductos().first()
        val productosStockBajo = productoRepository.obtenerProductosConStockBajo().first()

        val valorTotal = productos.sumOf { it.valorInventario }
        val margenPromedio = if (productos.isNotEmpty()) {
            productos.map { it.margenGanancia }.average()
        } else 0.0

        val productosSinMovimiento = productos.filter { producto ->
            producto.fechaUltimaVenta?.let { ultimaVenta ->
                System.currentTimeMillis() - ultimaVenta > 30L * 24 * 60 * 60 * 1000 // 30 días
            } ?: true
        }.size

        return MetricasInventario(
            totalProductos = productos.size,
            valorTotalInventario = valorTotal,
            productosStockBajo = productosStockBajo.size,
            productosSinMovimiento = productosSinMovimiento,
            margenPromedioGanancia = margenPromedio,
            topProductosVendidos = emptyList(), // Se puede implementar con más datos
            productosConMayorRotacion = emptyList() // Se puede implementar con más datos
        )
    }
}