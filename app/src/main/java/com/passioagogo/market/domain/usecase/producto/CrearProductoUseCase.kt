package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import javax.inject.Inject
import javax.inject.Singleton
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase

data class CrearProductoParams(
    val nombre: String = "",
    val descripcion: String? = null,
    val skuInterno: String = "",
    val codigoBarras: String? = null,
    val precioCompra: Double = 0.0,
    val precioVenta: Double = 0.0,
    val cantidadInicial: Int = 0,
    val cantidadMinima: Int = 0,
    val proveedorPrincipalId: Long? = null,
    val color: String? = null,
    val categorias: List<Long> = emptyList(),
    val subcategorias: List<Long> = emptyList(),
    val atributos: Map<String, String> = emptyMap()
)

@Singleton
class CrearProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<CrearProductoParams, Long>() {

    override suspend fun execute(parameters: CrearProductoParams): Long {
        // Validaciones de negocio
        validarDatosProducto(parameters)

        // Verificar duplicados
        if (parameters.skuInterno.isNotBlank()) {
            productoRepository.obtenerProductoPorSku(parameters.skuInterno)?.let {
                throw DomainException.SkuDuplicado(parameters.skuInterno)
            }
        }

        parameters.codigoBarras?.let { codigo ->
            if (codigo.isNotBlank()) {
                productoRepository.obtenerProductoPorCodigoBarras(codigo)?.let {
                    throw DomainException.CodigoBarrasDuplicado(codigo)
                }
            }
        }

        val producto = Producto(
            nombre = parameters.nombre,
            descripcion = parameters.descripcion,
            skuInterno = parameters.skuInterno,
            codigoBarras = parameters.codigoBarras,
            precioCompra = parameters.precioCompra,
            precioVenta = parameters.precioVenta,
            cantidadActual = parameters.cantidadInicial,
            cantidadMinima = parameters.cantidadMinima,
            cantidadMaximaComprada = parameters.cantidadInicial,
            proveedorPrincipalId = parameters.proveedorPrincipalId,
            color = parameters.color,
        )

        return when (val result = productoRepository.guardarProducto(producto)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }

    private fun validarDatosProducto(params: CrearProductoParams) {
        if (params.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del producto no puede estar vacío")
        }
        if (params.skuInterno.isBlank()) {
            throw IllegalArgumentException("El SKU interno no puede estar vacío")
        }
        if (params.precioCompra < 0) {
            throw DomainException.PrecioInvalido(params.precioCompra)
        }
        if (params.precioVenta < 0) {
            throw DomainException.PrecioInvalido(params.precioVenta)
        }
        if (params.cantidadInicial < 0) {
            throw DomainException.CantidadInvalida(params.cantidadInicial)
        }
        if (params.cantidadMinima < 0) {
            throw DomainException.CantidadInvalida(params.cantidadMinima)
        }
    }
}