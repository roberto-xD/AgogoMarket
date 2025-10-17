package com.passioagogo.market.domain.usecase.ventas

import com.passioagogo.market.data.repository.VentaRepository
import com.passioagogo.market.domain.model.venta.Venta
import javax.inject.Inject

class GuardarVentaUseCase @Inject constructor(
    private val repository: VentaRepository
) {
    suspend operator fun invoke(venta: Venta): Result<Long> {
        return repository.guardarVenta(venta)
    }
}