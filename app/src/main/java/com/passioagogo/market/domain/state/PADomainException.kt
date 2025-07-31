package com.passioagogo.market.domain.state

sealed class DomainException(message: String) : Exception(message) {
    class ProductoNoEncontrado(sku: String) : DomainException("Producto con SKU $sku no encontrado")
    class StockInsuficiente(producto: String, disponible: Int, requerido: Int) :
        DomainException("Stock insuficiente para $producto. Disponible: $disponible, Requerido: $requerido")
    class SkuDuplicado(sku: String) : DomainException("El SKU $sku ya existe")
    class CodigoBarrasDuplicado(codigo: String) : DomainException("El código de barras $codigo ya existe")
    class PrecioInvalido(precio: Double) : DomainException("Precio inválido: $precio")
    class CantidadInvalida(cantidad: Int) : DomainException("Cantidad inválida: $cantidad")
}