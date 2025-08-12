package com.passioagogo.market.domain.usecase.compras

import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import javax.inject.Inject
import javax.inject.Singleton

data class MovimientoStockParams(
    val productoId: Long,
    val cantidad: Int,
    val tipoMovimiento: TipoMovimiento,
    val motivo: String?
)

enum class TipoMovimiento {
    ENTRADA, SALIDA, AJUSTE
}

@Singleton
class GestionarStockUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<MovimientoStockParams, Unit>() {

    override suspend fun execute(parameters: MovimientoStockParams): Unit {
        val producto = productoRepository.obtenerProductoPorId(parameters.productoId)
            ?: throw DomainException.ProductoNoEncontrado("ID: ${parameters.productoId}")

        val nuevaCantidad = when (parameters.tipoMovimiento) {
            TipoMovimiento.ENTRADA -> {
                producto.cantidadActual + parameters.cantidad
            }
            TipoMovimiento.SALIDA -> {
                val nuevaCantidad = producto.cantidadActual - parameters.cantidad
                if (nuevaCantidad < 0) {
                    throw DomainException.StockInsuficiente(
                        producto.nombre,
                        producto.cantidadActual,
                        parameters.cantidad
                    )
                }
                nuevaCantidad
            }
            TipoMovimiento.AJUSTE -> {
                if (parameters.cantidad < 0) {
                    throw DomainException.CantidadInvalida(parameters.cantidad)
                }
                parameters.cantidad
            }
        }

        when (val result = productoRepository.actualizarStock(parameters.productoId, nuevaCantidad)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}