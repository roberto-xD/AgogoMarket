package com.passioagogo.market.domain.usecase.producto

import com.passioagogo.market.data.imports.GoogleSheetsImportService
import com.passioagogo.market.domain.repository.ICategoriaRepository
import com.passioagogo.market.domain.bean.CategoriaImport
import com.passioagogo.market.domain.bean.ErrorImportacion
import com.passioagogo.market.domain.bean.FamiliaImport
import com.passioagogo.market.domain.bean.ProductoImportExtendido
import com.passioagogo.market.domain.bean.ResultadoImportacionCompleta
import com.passioagogo.market.domain.bean.ResultadoImportacionProductos
import com.passioagogo.market.domain.bean.ResultadoImportacionItem
import com.passioagogo.market.domain.bean.SubcategoriaImport
import com.passioagogo.market.domain.bean.TipoErrorImportacion
import com.passioagogo.market.domain.repository.FamiliaRepository
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.repository.ISubcategoriaRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.base.UseCase
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaParams
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaUseCase
import com.passioagogo.market.domain.usecase.familias.CrearFamiliaParams
import com.passioagogo.market.domain.usecase.familias.CrearFamiliaUseCase
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaParams
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaUseCase
import jakarta.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

data class ImportarProductosParams(
    val spreadsheetUrl: String,
    val validarDuplicados: Boolean = true,
    val saltearDuplicados: Boolean = true,
    val actualizarExistentes: Boolean = false,
    val importarDatosMaestros: Boolean = true,
    val crearRelaciones: Boolean = true,
)

@Singleton
class ImportarProductosDesdeGoogleSheetsUseCase @Inject constructor(
    private val googleSheetsService: GoogleSheetsImportService,
    private val crearProductoUseCase: CrearProductoUseCase,
    private val crearFamiliaUseCase: CrearFamiliaUseCase,
    private val crearCategoriaUseCase: CrearCategoriaConFamiliaUseCase,
    private val crearSubcategoriaUseCase: CrearSubcategoriaConCategoriaUseCase,
    private val productoRepository: IProductoRepository,
    private val familiaRepository: FamiliaRepository,
    private val categoriaRepository: ICategoriaRepository,
    private val subcategoriaRepository: ISubcategoriaRepository,
) : UseCase<ImportarProductosParams, ResultadoImportacionCompleta>() {

    override suspend fun execute(parameters: ImportarProductosParams): ResultadoImportacionCompleta {
        val startTime = System.currentTimeMillis()

        // 1. Importar datos del spreadsheet
        val datosCompletos = when (val result = googleSheetsService.importarDatos(parameters.spreadsheetUrl)) {
            is PADomainState.Success -> result.data
            is PADomainState.Error -> throw result.exception
            is PADomainState.Loading -> throw IllegalStateException("Importación en proceso")
        }

        if (datosCompletos.productos.isEmpty()) {
            throw Exception("No se encontraron productos para importar")
        }

        // 2. Crear mapas para almacenar IDs generados
        val familiasIds = mutableMapOf<String, Long>()
        val categoriasIds = mutableMapOf<String, Long>()
        val subcategoriasIds = mutableMapOf<String, Long>()

        // 3. Importar familias si están habilitadas
        val resultadoFamilias = if (parameters.importarDatosMaestros && datosCompletos.familias.isNotEmpty()) {
            importarFamilias(datosCompletos.familias, familiasIds)
        } else {
            ResultadoImportacionItem(0, 0, 0, emptyList())
        }

        // 4. Importar categorías si están habilitadas
        val resultadoCategorias = if (parameters.importarDatosMaestros && datosCompletos.categorias.isNotEmpty()) {
            importarCategorias(datosCompletos.categorias, familiasIds, categoriasIds)
        } else {
            ResultadoImportacionItem(0, 0, 0, emptyList())
        }

        // 5. Importar subcategorías si están habilitadas
        val resultadoSubcategorias = if (parameters.importarDatosMaestros && datosCompletos.subcategorias.isNotEmpty()) {
            importarSubcategorias(datosCompletos.subcategorias, categoriasIds, subcategoriasIds)
        } else {
            ResultadoImportacionItem(0, 0, 0, emptyList())
        }

        // 6. Importar productos con relaciones
        val resultadoProductos = importarProductosConRelaciones(
            datosCompletos.productosExtendidos,
            parameters,
            familiasIds,
            categoriasIds,
            subcategoriasIds
        )

        val endTime = System.currentTimeMillis()

        return ResultadoImportacionCompleta(
            totalProcesados = resultadoProductos.totalProcesados,
            exitosos = resultadoProductos.exitosos,
            fallidos = resultadoProductos.fallidos,
            errores = resultadoProductos.errores,
            duplicadosEncontrados = resultadoProductos.duplicadosEncontrados,
            tiempoTranscurrido = endTime - startTime,
            resultadoFamilias = resultadoFamilias,
            resultadoCategorias = resultadoCategorias,
            resultadoSubcategorias = resultadoSubcategorias,
            relacionesCreadas = calcularRelacionesCreadas(datosCompletos.productosExtendidos)
        )
    }

    private suspend fun importarFamilias(
        familias: List<FamiliaImport>,
        familiasIds: MutableMap<String, Long>
    ): ResultadoImportacionItem {
        var exitosos = 0
        val errores = mutableListOf<ErrorImportacion>()

        familias.forEach { familiaImport ->
            try {
                // Verificar si ya existe
                val familiaExistente = familiaRepository.obtenerTodasLasFamilias().first()
                    .find { it.nombre.equals(familiaImport.nombre, ignoreCase = true) }

                val familiaId = if (familiaExistente != null) {
                    familiaExistente.id
                } else {
                    val params = CrearFamiliaParams(
                        nombre = familiaImport.nombre,
                        descripcion = familiaImport.descripcion
                    )

                    when (val result = crearFamiliaUseCase(params)) {
                        is PADomainState.Success -> result.data
                        is PADomainState.Error -> throw result.exception
                        is PADomainState.Loading -> throw IllegalStateException("Operación en proceso")
                    }
                }

                familiasIds[familiaImport.nombre] = familiaId
                exitosos++

            } catch (e: Exception) {
                errores.add(ErrorImportacion(
                    numeroFila = 0,
                    sku = familiaImport.nombre,
                    mensaje = e.message ?: "Error desconocido",
                    tipo = TipoErrorImportacion.ERROR_BASE_DATOS
                ))
            }
        }

        return ResultadoImportacionItem(
            totalProcesados = familias.size,
            exitosos = exitosos,
            fallidos = errores.size,
            errores = errores
        )
    }

    private suspend fun importarCategorias(
        categorias: List<CategoriaImport>,
        familiasIds: Map<String, Long>,
        categoriasIds: MutableMap<String, Long>
    ): ResultadoImportacionItem {
        var exitosos = 0
        val errores = mutableListOf<ErrorImportacion>()

        categorias.forEach { categoriaImport ->
            try {
                // Resolver familia padre
                val familiaId = categoriaImport.familiaNombre?.let { familiasIds[it] }
                    ?: throw Exception("Familia '${categoriaImport.familiaNombre}' no encontrada")

                // Verificar si ya existe
                val categoriaExistente = categoriaRepository.obtenerTodasLasCategorias().first()
                    .find { it.nombre.equals(categoriaImport.nombre, ignoreCase = true) && it.familiaId == familiaId }

                val categoriaId = if (categoriaExistente != null) {
                    categoriaExistente.id
                } else {
                    val params = CrearCategoriaConFamiliaParams(
                        nombre = categoriaImport.nombre,
                        descripcion = categoriaImport.descripcion,
                        familiaId = familiaId
                    )

                    when (val result = crearCategoriaUseCase(params)) {
                        is PADomainState.Success -> result.data
                        is PADomainState.Error -> throw result.exception
                        is PADomainState.Loading -> throw IllegalStateException("Operación en proceso")
                    }
                }

                categoriasIds[categoriaImport.nombre] = categoriaId
                exitosos++

            } catch (e: Exception) {
                errores.add(ErrorImportacion(
                    numeroFila = 0,
                    sku = categoriaImport.nombre,
                    mensaje = e.message ?: "Error desconocido",
                    tipo = TipoErrorImportacion.ERROR_BASE_DATOS
                ))
            }
        }

        return ResultadoImportacionItem(
            totalProcesados = categorias.size,
            exitosos = exitosos,
            fallidos = errores.size,
            errores = errores
        )
    }

    private suspend fun importarSubcategorias(
        subcategorias: List<SubcategoriaImport>,
        categoriasIds: Map<String, Long>,
        subcategoriasIds: MutableMap<String, Long>
    ): ResultadoImportacionItem {
        var exitosos = 0
        val errores = mutableListOf<ErrorImportacion>()

        subcategorias.forEach { subcategoriaImport ->
            try {
                // Resolver categoría padre
                val categoriaId = subcategoriaImport.categoriaNombre?.let { categoriasIds[it] }
                    ?: throw Exception("Categoría '${subcategoriaImport.categoriaNombre}' no encontrada")

                // Verificar si ya existe
                val subcategoriaExistente = subcategoriaRepository.obtenerTodasLasSubcategorias().first()
                    .find { it.nombre.equals(subcategoriaImport.nombre, ignoreCase = true) && it.categoriaId == categoriaId }

                val subcategoriaId = if (subcategoriaExistente != null) {
                    subcategoriaExistente.id
                } else {
                    val params = CrearSubcategoriaConCategoriaParams(
                        nombre = subcategoriaImport.nombre,
                        descripcion = subcategoriaImport.descripcion,
                        categoriaId = categoriaId
                    )

                    when (val result = crearSubcategoriaUseCase(params)) {
                        is PADomainState.Success -> result.data
                        is PADomainState.Error -> throw result.exception
                        is PADomainState.Loading -> throw IllegalStateException("Operación en proceso")
                    }
                }

                subcategoriasIds[subcategoriaImport.nombre] = subcategoriaId
                exitosos++

            } catch (e: Exception) {
                errores.add(ErrorImportacion(
                    numeroFila = 0,
                    sku = subcategoriaImport.nombre,
                    mensaje = e.message ?: "Error desconocido",
                    tipo = TipoErrorImportacion.ERROR_BASE_DATOS
                ))
            }
        }

        return ResultadoImportacionItem(
            totalProcesados = subcategorias.size,
            exitosos = exitosos,
            fallidos = errores.size,
            errores = errores
        )
    }

    private suspend fun importarProductosConRelaciones(
        productos: List<ProductoImportExtendido>,
        parameters: ImportarProductosParams,
        familiasIds: Map<String, Long>,
        categoriasIds: Map<String, Long>,
        subcategoriasIds: Map<String, Long>
    ): ResultadoImportacionProductos {
        var exitosos = 0
        var duplicadosEncontrados = 0
        val errores = mutableListOf<ErrorImportacion>()

        productos.forEach { productoExtendido ->
            try {
                val producto = productoExtendido.productoBase

                // Validar duplicados si está habilitado
                val productoExistente = productoRepository.obtenerProductoPorSku(producto.skuInterno)

                if (productoExistente != null) {
                    duplicadosEncontrados++

                    if (parameters.saltearDuplicados) {
                        errores.add(ErrorImportacion(
                            numeroFila = producto.numeroFila,
                            sku = producto.skuInterno,
                            mensaje = "SKU ya existe - saltado",
                            tipo = TipoErrorImportacion.SKU_DUPLICADO
                        ))
                        return@forEach
                    }
                }

                // Resolver IDs de relaciones
                val categoriaIds = productoExtendido.categoriasNombres.mapNotNull { nombre ->
                    categoriasIds[nombre]
                }

                val subcategoriaIds = productoExtendido.subcategoriasNombres.mapNotNull { nombre ->
                    subcategoriasIds[nombre]
                }

                val familiaId = productoExtendido.familiasNombres.map { nombre ->
                    familiasIds[nombre]
                }.firstOrNull()

                // Crear producto con relaciones
                val params = productoExtendido.toCrearProductoParamsExtendido(
                    familiaId = familiaId,
                    categoriaIds = categoriaIds,
                    subcategoriaIds = subcategoriaIds
                )

                when (val resultado = crearProductoUseCase(params)) {
                    is PADomainState.Success -> {
                        exitosos++
                    }
                    is PADomainState.Error -> {
                        val tipoError = when (resultado.exception) {
                            is DomainException.SkuDuplicado -> TipoErrorImportacion.SKU_DUPLICADO
                            is DomainException.CodigoBarrasDuplicado -> TipoErrorImportacion.CODIGO_BARRAS_DUPLICADO
                            else -> TipoErrorImportacion.ERROR_BASE_DATOS
                        }

                        errores.add(ErrorImportacion(
                            numeroFila = producto.numeroFila,
                            sku = producto.skuInterno,
                            mensaje = resultado.exception.message ?: "Error desconocido",
                            tipo = tipoError
                        ))
                    }
                    is PADomainState.Loading -> {
                        // No debería ocurrir
                    }
                }

            } catch (e: Exception) {
                errores.add(ErrorImportacion(
                    numeroFila = productoExtendido.productoBase.numeroFila,
                    sku = productoExtendido.productoBase.skuInterno,
                    mensaje = e.message ?: "Error inesperado",
                    tipo = TipoErrorImportacion.ERROR_BASE_DATOS
                ))
            }
        }

        return ResultadoImportacionProductos(
            totalProcesados = productos.size,
            exitosos = exitosos,
            fallidos = errores.size,
            errores = errores,
            duplicadosEncontrados = duplicadosEncontrados,
            tiempoTranscurrido = 0L // Se calculará en el método principal
        )
    }

    private fun calcularRelacionesCreadas(productos: List<ProductoImportExtendido>): Int {
        return productos.sumOf { producto ->
            producto.categoriasNombres.size + producto.subcategoriasNombres.size
        }
    }
}