package com.passioagogo.market.domain.usecase.ventas

import com.passioagogo.market.data.repository.VentaRepository
import com.passioagogo.market.domain.model.venta.Venta
import javax.inject.Inject

class ObtenerVentaUseCase @Inject constructor(
    private val repository: VentaRepository
) {
    suspend operator fun invoke(id: Long): Result<Venta> {
        return repository.obtenerVenta(id)
    }
}