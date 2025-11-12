package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrearProductoUseCase @Inject constructor(
    private val productoRepository: IProductoRepository
) : UseCase<GuardarProductoParams, Long>() {

    override suspend fun execute(parameters: GuardarProductoParams): Long {
        // Validaciones de negocio
        validarDatosObligatorios(parameters)

        // Verificar duplicados
//        if (parameters.skuInterno.isNotBlank()) {
//            productoRepository.obtenerProductoPorSku(parameters.skuInterno)?.let {
//                throw DomainException.SkuDuplicado(parameters.skuInterno)
//            }
//        }

//        parameters.codigoBarras?.let { codigo ->
//            if (codigo.isNotBlank()) {
//                productoRepository.obtenerProductoPorCodigoBarras(codigo)?.let {
//                    throw DomainException.CodigoBarrasDuplicado(codigo)
//                }
//            }
//        }

        val producto = Producto(
            nombre = parameters.nombre,
            descripcion = parameters.descripcion,
            skuInterno = parameters.skuInterno,
            codigoBarras = parameters.codigoBarras,
            precioCompra = parameters.precioCompra,
            precioVenta = parameters.precioVenta,
            cantidadActual = parameters.cantidadActual,
            cantidadMinima = parameters.cantidadMinima,
            cantidadMaximaComprada = parameters.cantidadInicial,
            proveedorPrincipalId = parameters.proveedorPrincipalId,
        )
        val result = if(parameters.familiaId == 0L){
            productoRepository.guardarProducto(producto)
        }else {
            val productoDetallado = ProductoDetallado(
                producto = producto,
                idFamilia = parameters.familiaId,
                idCategoria = parameters.categoriaId,
                idSubCategoria = parameters.subcategoriaId,
                imagenes = parameters.imagenes,
            )
            productoRepository.guardarProductoDetallado(productoDetallado)
        }


        return when (result) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Operación en estado loading")
        }
    }

    private fun validarDatosObligatorios(params: GuardarProductoParams) {
        if (params.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del producto no puede estar vacío")
        }
        if(params.cantidadActual == 0){
            throw IllegalArgumentException("La cantidad actual no puede ser cero")
        }
        if (params.precioVenta < 0) {
            throw DomainException.PrecioInvalido(params.precioVenta)
        }
    }
}