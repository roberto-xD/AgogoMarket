package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.domain.bean.HistorialPrecio
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IHistorialRepository
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualizarProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository,
    private val historialRepository: IHistorialRepository
) : UseCase<Producto, Unit>() {

    override suspend fun execute(parameters: Producto): Unit {
        val productoActual = productoRepository.obtenerProductoPorId(parameters.id)
            ?: throw DomainException.ProductoNoEncontrado(parameters.skuInterno)

        // Registrar cambio de precios si es necesario
        if (productoActual.precioCompra != parameters.precioCompra ||
            productoActual.precioVenta != parameters.precioVenta) {

            val historial = HistorialPrecio(
                productoId = parameters.id,
                precioCompraAnterior = productoActual.precioCompra,
                precioVentaAnterior = productoActual.precioVenta,
                precioCompraNuevo = parameters.precioCompra,
                precioVentaNuevo = parameters.precioVenta,
                motivo = "Actualización manual"
            )

            historialRepository.registrarCambioPrecio(historial)
        }

        when (val result = productoRepository.actualizarProducto(parameters)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }
}