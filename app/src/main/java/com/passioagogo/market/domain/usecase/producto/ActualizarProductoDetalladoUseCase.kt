package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import com.passioagogo.market.ui.decorators.orZero
import javax.inject.Inject
import javax.inject.Singleton


data class GuardarProductoParams(
    val id : Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val skuInterno: String = "",
    val codigoBarras: String = "",
    val precioCompra: Double = 0.0,
    val precioVenta: Double = 0.0,
    val cantidadInicial: Int = 0,
    val cantidadMinima: Int = 0,
    val cantidadActual: Int = 0,
    val proveedorPrincipalId: Long? = null,
    val imagenes: List<ImagenProducto> = emptyList(),
    val familiaId: Long = 0L,
    val categoriaId: Long = 0L,
    val subcategoriaId: Long = 0L,
    val atributos: Map<String, String> = emptyMap()
)
@Singleton
class ActualizarProductoDetalladoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<GuardarProductoParams, Unit>() {

    override suspend fun execute(parameters: GuardarProductoParams) {
        // Validaciones de negocio
        validarDatosProducto(parameters)

        val producto = Producto(
            id = parameters.id,
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
        )
        val productoDetallado = ProductoDetallado(
            producto = producto,
            idFamilia = parameters.familiaId.orZero(),
            idCategoria = parameters.categoriaId,
            idSubCategoria = parameters.subcategoriaId,
            imagenes = parameters.imagenes,
        )

        return when (val result = productoRepository.actualizarProductoDetallado(productoDetallado)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }

    private fun validarDatosProducto(params: GuardarProductoParams) {
        if (params.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del producto no puede estar vacío")
        }
//        if (params.skuInterno.isBlank()) {
//            throw IllegalArgumentException("El SKU interno no puede estar vacío")
//        }
//        if (params.precioCompra < 0) {
//            throw DomainException.PrecioInvalido(params.precioCompra)
//        }
        if (params.precioVenta < 0) {
            throw DomainException.PrecioInvalido(params.precioVenta)
        }
//        if (params.cantidadInicial < 0) {
//            throw DomainException.CantidadInvalida(params.cantidadInicial)
//        }
//        if (params.cantidadMinima < 0) {
//            throw DomainException.CantidadInvalida(params.cantidadMinima)
//        }
    }
}