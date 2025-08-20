package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.data.imports.GoogleSheetsImportService
import com.passioagogo.market.domain.bean.ErrorImportacion
import com.passioagogo.market.domain.bean.ProductoImport
import com.passioagogo.market.domain.bean.ResultadoImportacion
import com.passioagogo.market.domain.bean.TipoErrorImportacion
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class ImportarProductosParams(
    val spreadsheetUrl: String,
    val validarDuplicados: Boolean = true,
    val saltearDuplicados: Boolean = true,
    val actualizarExistentes: Boolean = false
)

@Singleton
class ImportarProductosDesdeGoogleSheetsUseCase @Inject constructor(
    private val googleSheetsService: GoogleSheetsImportService,
    private val crearProductoUseCase: CrearProductoUseCase,
    private val productoRepository: IProductoRepository
) : UseCase<ImportarProductosParams, ResultadoImportacion>() {

    override suspend fun execute(parameters: ImportarProductosParams): ResultadoImportacion {
        val startTime = System.currentTimeMillis()

        // 1. Importar datos del spreadsheet
        val productosImport = when (val result = googleSheetsService.importarProductos(parameters.spreadsheetUrl)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Importación en proceso")
        }

        if (productosImport.isEmpty()) {
            throw Exception("No se encontraron productos para importar")
        }

        // 2. Validar duplicados si está habilitado
        val productosParaProcesar = if (parameters.validarDuplicados) {
            validarYFiltrarDuplicados(productosImport, parameters)
        } else {
            productosImport
        }

        // 3. Procesar productos uno por uno
        var exitosos = 0
        var duplicadosEncontrados = 0
        val errores = mutableListOf<ErrorImportacion>()

        productosParaProcesar.forEach { productoImport ->
            try {
                // Validar que no existe el SKU
                val productoExistente = productoRepository.obtenerProductoPorSku(productoImport.skuInterno)

                if (productoExistente != null) {
                    duplicadosEncontrados++

                    if (parameters.saltearDuplicados) {
                        errores.add(ErrorImportacion(
                            numeroFila = productoImport.numeroFila,
                            sku = productoImport.skuInterno,
                            mensaje = "SKU ya existe - saltado",
                            tipo = TipoErrorImportacion.SKU_DUPLICADO
                        ))
                        return@forEach
                    } else if (parameters.actualizarExistentes) {
                        // TODO: Implementar actualización de productos existentes
                        // actualizarProductoUseCase(productoExistente.copy(...))
                        exitosos++
                        return@forEach
                    }
                }

                // Crear el producto
                val params = productoImport.toCrearProductoParams()

                when (val resultado = crearProductoUseCase(params)) {
                    is PADomainState.Success -> {
                        exitosos++
                    }
                    is PADomainState.Error -> {
                        val tipoError = when (resultado.exception) {
                            is DomainException.SkuDuplicado -> TipoErrorImportacion.SKU_DUPLICADO
                            is DomainException.CodigoBarrasDuplicado -> TipoErrorImportacion.CODIGO_BARRAS_DUPLICADO
                            is DomainException.PrecioInvalido,
                            is DomainException.CantidadInvalida -> TipoErrorImportacion.VALIDACION_NEGOCIO
                            else -> TipoErrorImportacion.ERROR_BASE_DATOS
                        }

                        errores.add(ErrorImportacion(
                            numeroFila = productoImport.numeroFila,
                            sku = productoImport.skuInterno,
                            mensaje = resultado.exception.message ?: "Error desconocido",
                            tipo = tipoError
                        ))
                    }
                    is PADomainState.Loading -> {
                        // No debería ocurrir en este contexto
                    }
                }

            } catch (e: Exception) {
                errores.add(ErrorImportacion(
                    numeroFila = productoImport.numeroFila,
                    sku = productoImport.skuInterno,
                    mensaje = e.message ?: "Error inesperado",
                    tipo = TipoErrorImportacion.ERROR_BASE_DATOS
                ))
            }
        }

        val endTime = System.currentTimeMillis()

        return ResultadoImportacion(
            totalProcesados = productosParaProcesar.size,
            exitosos = exitosos,
            fallidos = errores.size,
            errores = errores,
            duplicadosEncontrados = duplicadosEncontrados,
            tiempoTranscurrido = endTime - startTime
        )
    }

    private suspend fun validarYFiltrarDuplicados(
        productos: List<ProductoImport>,
        parameters: ImportarProductosParams
    ): List<ProductoImport> {
        return if (parameters.saltearDuplicados) {
            // Filtrar duplicados por SKU dentro del mismo archivo
            val skusVistos = mutableSetOf<String>()
            productos.filter { producto ->
                if (skusVistos.contains(producto.skuInterno)) {
                    false
                } else {
                    skusVistos.add(producto.skuInterno)
                    true
                }
            }
        } else {
            productos
        }
    }
}