package com.passioagogo.market.domain.usecase.ventas

import com.passioagogo.market.data.repository.VentaRepository
import com.passioagogo.market.domain.model.venta.ProductoVenta
import javax.inject.Inject

class ValidarStockUseCase @Inject constructor(
    private val repository: VentaRepository
) {
    suspend operator fun invoke(productos: List<ProductoVenta>): Result<Unit> {
        return repository.validarStock(productos)
    }
}