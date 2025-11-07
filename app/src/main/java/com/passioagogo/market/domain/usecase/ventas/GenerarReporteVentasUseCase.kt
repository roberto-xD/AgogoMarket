package com.passioagogo.market.domain.usecase.ventas

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.ICategoriaRepository
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class GenerarReporteParams(
    val fechaInicio: Long,
    val fechaFin: Long,
    val categoriaId: Long? = null,
    val proveedorId: Long? = null
)

data class ReporteVentas(
    val periodo: String,
    val totalVentas: Double,
    val cantidadProductosVendidos: Int,
    val productoMasVendido: Producto?,
    val categoriaConMasVentas: Categoria?,
    val detallesPorProducto: List<DetalleVentaProducto>
)

data class DetalleVentaProducto(
    val producto: Producto,
    val cantidadVendida: Int,
    val ingresoTotal: Double,
    val gananciaTotal: Double
)

@Singleton
class GenerarReporteVentasUseCase @Inject constructor(
    private val productoRepository: IProductoRepository,
    private val categoriaRepository: ICategoriaRepository
) : UseCase<GenerarReporteParams, ReporteVentas>() {

    override suspend fun execute(parameters: GenerarReporteParams): ReporteVentas {
        // Esta implementación es básica, se puede expandir con más datos históricos
        val productos = productoRepository.obtenerTodosLosProductos().first()

        return ReporteVentas(
            periodo = "Reporte básico",
            totalVentas = 0.0,
            cantidadProductosVendidos = 0,
            productoMasVendido = null,
            categoriaConMasVentas = null,
            detallesPorProducto = emptyList()
        )
    }
}