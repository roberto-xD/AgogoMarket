package com.passioagogo.market.data.imports

import com.google.gson.Gson
import com.passioagogo.market.domain.bean.CategoriaImport
import com.passioagogo.market.domain.bean.DatosImportCompletos
import com.passioagogo.market.domain.bean.EstructuraValidacionCompleta
import com.passioagogo.market.domain.bean.FamiliaImport
import com.passioagogo.market.domain.bean.ProductoImport
import com.passioagogo.market.domain.bean.ProductoImportExtendido
import com.passioagogo.market.domain.bean.RelacionesDetectadas
import com.passioagogo.market.domain.bean.SubcategoriaImport
import com.passioagogo.market.domain.bean.ValidacionEstructura
import com.passioagogo.market.domain.bean.VistaPreviaCompleta
import com.passioagogo.market.domain.state.PADomainState
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Singleton

interface GoogleSheetsImportService {
    suspend fun importarDatos(spreadsheetUrl: String): PADomainState<DatosImportCompletos>
    suspend fun validarEstructura(spreadsheetUrl: String): PADomainState<EstructuraValidacionCompleta>
    suspend fun obtenerVistaPrevia(spreadsheetUrl: String, limite: Int = 5): PADomainState<VistaPreviaCompleta>
}

@Singleton
class GoogleSheetsImportServiceImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) : GoogleSheetsImportService {

    companion object {
        // Encabezados esperados en el spreadsheet (orden importa)
        private val HEADERS_ESPERADOS = listOf(
            "nombre", "descripcion", "skuInterno",
            "precioCompra", "precioVenta", "cantidadActual", "cantidadMinima",
            "cantidadMaximaComprada", "color", "activo", "familias",
            "categorias", "subcategorias", "atributos"
        )
        private val HEADERS_OBLIGATORIOS = listOf("nombre", "precioventa")
        private val HEADERS_FAMILIAS = listOf("familia_id", "familia_nombre", "familia_descripcion")
        private val HEADERS_CATEGORIAS = listOf("categoria_id", "categoria_nombre", "categoria_descripcion", "categoria_familia")
        private val HEADERS_SUBCATEGORIAS = listOf("subcategoria_id", "subcategoria_nombre", "subcategoria_descripcion", "subcategoria_categoria")

    }

override suspend fun importarDatos(spreadsheetUrl: String): PADomainState<DatosImportCompletos> {
        return try {
            val csvUrl = convertirACsvUrl(spreadsheetUrl)
            val csvContent = descargarCsv(csvUrl)
            val productos = parsearCsv(csvContent)

            PADomainState.Success(productos)

        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun validarEstructura(spreadsheetUrl: String): PADomainState<EstructuraValidacionCompleta> {
        return try {
            val csvUrl = convertirACsvUrl(spreadsheetUrl)
            val csvContent = descargarCsv(csvUrl)
            val lineas = csvContent.lines()

            if (lineas.isEmpty()) {
                return PADomainState.Error(Exception("El archivo está vacío"))
            }

            // Obtener headers del archivo
            val headersArchivo = parsearLineaCsv(lineas.first())
                .map { it.trim().lowercase() }

            // Validar headers obligatorios
            val headersFaltantes = HEADERS_OBLIGATORIOS.filter { header ->
                !headersArchivo.contains(header.lowercase())
            }
            // Detectar si hay datos maestros
            val tieneFamilias = HEADERS_FAMILIAS.any { headersArchivo.contains(it) }
            val tieneCategorias = HEADERS_CATEGORIAS.any { headersArchivo.contains(it) }
            val tieneSubcategorias = HEADERS_SUBCATEGORIAS.any { headersArchivo.contains(it) }

            // Validar relaciones en productos
            val tieneRelacionFamilias = headersArchivo.contains("familias")
            val tieneRelacionCategorias = headersArchivo.contains("categorias")
            val tieneRelacionSubcategorias = headersArchivo.contains("subcategorias")


            val validacion = EstructuraValidacionCompleta(
                esValida = headersFaltantes.isEmpty(),
                headersFaltantes = headersFaltantes,
                headersEncontrados = headersArchivo,
                totalFilas = lineas.size - 1,
                tieneDatosMaestros = tieneFamilias || tieneCategorias || tieneSubcategorias,
                estructuraFamilias = if (tieneFamilias) validarEstructuraFamilias(headersArchivo) else null,
                estructuraCategorias = if (tieneCategorias) validarEstructuraCategorias(
                    headersArchivo
                ) else null,
                estructuraSubcategorias = if (tieneSubcategorias) validarEstructuraSubcategorias(
                    headersArchivo
                ) else null,
                tieneRelaciones = tieneRelacionFamilias || tieneRelacionCategorias || tieneRelacionSubcategorias,
                relacionesDetectadas = RelacionesDetectadas(
                    familias = tieneRelacionFamilias,
                    categorias = tieneRelacionCategorias,
                    subcategorias = tieneRelacionSubcategorias
                )
            )

            PADomainState.Success(validacion)

        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun obtenerVistaPrevia(
        spreadsheetUrl: String,
        limite: Int
    ): PADomainState<VistaPreviaCompleta> {
        return try {
            val datosCompletos = when (val result = importarDatos(spreadsheetUrl)) {
                is PADomainState.Success -> result.data
                is PADomainState.Error -> return PADomainState.Error(result.exception)
                is PADomainState.Loading -> return PADomainState.Error(Exception("Carga en proceso"))
            }

            val vistaPrevia = VistaPreviaCompleta(
                productos = datosCompletos.productos.take(limite),
                familias = datosCompletos.familias.take(limite),
                categorias = datosCompletos.categorias.take(limite),
                subcategorias = datosCompletos.subcategorias.take(limite),
                totalProductos = datosCompletos.productos.size,
                totalFamilias = datosCompletos.familias.size,
                totalCategorias = datosCompletos.categorias.size,
                totalSubcategorias = datosCompletos.subcategorias.size
            )

            PADomainState.Success(vistaPrevia)

        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    private suspend fun descargarCsv(url: String): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Error al descargar: ${response.code} - ${response.message}")
            }

            response.body?.string() ?: throw Exception("Contenido vacío")
        }
    }

    private fun convertirACsvUrl(spreadsheetUrl: String): String {
        // Convertir URL de Google Sheets a formato CSV exportable
        return when {
            spreadsheetUrl.contains("/edit") -> {
                // URL típica: https://docs.google.com/spreadsheets/d/SHEET_ID/edit#gid=0
                val sheetId = extractSheetId(spreadsheetUrl)
                "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv&gid=0"
            }
            spreadsheetUrl.contains("/export") -> {
                // Ya es una URL de exportación
                spreadsheetUrl
            }
            else -> {
                throw Exception("URL de Google Sheets no válida. Use el enlace de 'Compartir' del documento.")
            }
        }
    }

    private fun extractSheetId(url: String): String {
        val regex = "/spreadsheets/d/([a-zA-Z0-9-_]+)".toRegex()
        return regex.find(url)?.groupValues?.get(1)
            ?: throw Exception("No se pudo extraer el ID del spreadsheet")
    }

    private fun parsearCsv(csvContent: String, limite: Int? = null): DatosImportCompletos {
        val lineas = csvContent.lines()

        if (lineas.size < 2) {
            throw Exception("El archivo debe contener al menos una fila de datos además del encabezado")
        }

        val headers = parsearLineaCsv(lineas.first()).map { it.trim().lowercase() }
        val filasData = lineas.drop(1).filter { it.trim().isNotEmpty() }

        val familiasMap = mutableMapOf<String, FamiliaImport>()
        val categoriasMap = mutableMapOf<String, CategoriaImport>()
        val subcategoriasMap = mutableMapOf<String, SubcategoriaImport>()
        val productos = mutableListOf<ProductoImportExtendido>()

        filasData.forEachIndexed { index, linea ->
            try {
                val valores = parsearLineaCsv(linea)
                val mapa = headers.zip(valores).toMap()

                // Extraer familia si existe
                extraerFamilia(mapa, familiasMap)

                // Extraer categoría si existe
                extraerCategoria(mapa, categoriasMap)

                // Extraer subcategoría si existe
                extraerSubcategoria(mapa, subcategoriasMap)

                // Parsear producto con relaciones
                val producto = parsearProductoExtendido(headers, valores, index + 2)
                productos.add(producto)

            } catch (e: Exception) {
                throw Exception("Error en fila ${index + 2}: ${e.message}")
            }
        }

        return DatosImportCompletos(
            productos = productos.map { it.toProductoImport() },
            familias = familiasMap.values.toList(),
            categorias = categoriasMap.values.toList(),
            subcategorias = subcategoriasMap.values.toList(),
            productosExtendidos = productos
        )
    }

    private fun extraerFamilia(mapa: Map<String, String>, familiasMap: MutableMap<String, FamiliaImport>) {
        val nombre = mapa["familia_nombre"]?.trim() ?: mapa["familias"]?.split(",")?.firstOrNull()?.trim()
        if (!nombre.isNullOrEmpty() && !familiasMap.containsKey(nombre)) {
            familiasMap[nombre] = FamiliaImport(
                id = mapa["familia_id"]?.toLongOrNull() ?: 0L,
                nombre = nombre,
                descripcion = mapa["familia_descripcion"]?.trim() ?: "",
                activo = true
            )
        }
    }

    private fun extraerCategoria(mapa: Map<String, String>, categoriasMap: MutableMap<String, CategoriaImport>) {
        val nombre = mapa["categoria_nombre"]?.trim() ?: mapa["categorias"]?.split(",")?.firstOrNull()?.trim()
        if (!nombre.isNullOrEmpty() && !categoriasMap.containsKey(nombre)) {
            categoriasMap[nombre] = CategoriaImport(
                id = mapa["categoria_id"]?.toLongOrNull() ?: 0L,
                nombre = nombre,
                descripcion = mapa["categoria_descripcion"]?.trim(),
                familiaNombre = mapa["categoria_familia"]?.trim() ?: mapa["familias"]?.split(",")?.firstOrNull()?.trim(),
                activo = true
            )
        }
    }

    private fun extraerSubcategoria(mapa: Map<String, String>, subcategoriasMap: MutableMap<String, SubcategoriaImport>) {
        val nombre = mapa["subcategoria_nombre"]?.trim() ?: mapa["subcategorias"]?.split(",")?.firstOrNull()?.trim()
        if (!nombre.isNullOrEmpty() && !subcategoriasMap.containsKey(nombre)) {
            subcategoriasMap[nombre] = SubcategoriaImport(
                id = mapa["subcategoria_id"]?.toLongOrNull() ?: 0L,
                nombre = nombre,
                descripcion = mapa["subcategoria_descripcion"]?.trim(),
                categoriaNombre = mapa["subcategoria_categoria"]?.trim() ?: mapa["categorias"]?.split(",")?.firstOrNull()?.trim(),
                activo = true
            )
        }
    }

    private fun validarEstructuraFamilias(headers: List<String>): ValidacionEstructura {
        val headersFaltantes = HEADERS_FAMILIAS.filter { !headers.contains(it) }
        return ValidacionEstructura(
            esValida = headers.contains("familia_nombre"),
            headersFaltantes = headersFaltantes,
            headersEncontrados = HEADERS_FAMILIAS.filter { headers.contains(it) }
        )
    }

    private fun validarEstructuraCategorias(headers: List<String>): ValidacionEstructura {
        val headersFaltantes = HEADERS_CATEGORIAS.filter { !headers.contains(it) }
        return ValidacionEstructura(
            esValida = headers.contains("categoria_nombre"),
            headersFaltantes = headersFaltantes,
            headersEncontrados = HEADERS_CATEGORIAS.filter { headers.contains(it) }
        )
    }

    private fun validarEstructuraSubcategorias(headers: List<String>): ValidacionEstructura {
        val headersFaltantes = HEADERS_SUBCATEGORIAS.filter { !headers.contains(it) }
        return ValidacionEstructura(
            esValida = headers.contains("subcategoria_nombre"),
            headersFaltantes = headersFaltantes,
            headersEncontrados = HEADERS_SUBCATEGORIAS.filter { headers.contains(it) }
        )
    }

    private fun parsearProductoExtendido(
        headers: List<String>,
        valores: List<String>,
        numeroFila: Int
    ): ProductoImportExtendido {
        val mapa = headers.zip(valores).toMap()

        val productoBase = ProductoImport(
            numeroFila = numeroFila,
            id = mapa["id"]?.toLongOrNull() ?: 0L,
            nombre = mapa["nombre"]?.trim() ?: throw Exception("Nombre es obligatorio"),
            descripcion = mapa["descripcion"]?.trim()?.takeIf { it.isNotEmpty() },
            skuInterno = mapa["skuinterno"]?.trim() ?: "", //throw Exception("SKU interno es obligatorio"),
            codigoBarras = mapa["codigobarras"]?.trim()?.takeIf { it.isNotEmpty() },
            precioCompra = mapa["preciocompra"]?.toDoubleOrNull() ?: 1.0 ,//throw Exception("Precio de compra inválido"),
            precioVenta = mapa["precioventa"]?.toDoubleOrNull() ?: throw Exception("Precio de venta inválido"),
            cantidadActual = mapa["cantidadactual"]?.toIntOrNull() ?: 0,
            cantidadMinima = mapa["cantidadminima"]?.toIntOrNull() ?: 0,
            cantidadMaximaComprada = mapa["cantidadmaxima"]?.toIntOrNull() ?: 0,
            proveedorPrincipalId = mapa["proveedorprincipalid"]?.toLongOrNull(),
            color = mapa["color"]?.trim()?.takeIf { it.isNotEmpty() },
            fechaUltimaVenta = mapa["fechaultimaventa"]?.toLongOrNull(),
            fechaUltimaCompra = mapa["fechaultimacompra"]?.toLongOrNull(),
            activo = mapa["activo"]?.lowercase()?.let {
                it == "true" || it == "1" || it == "sí" || it == "si"
            } ?: true,
            erroresValidacion = mutableListOf()
        )

        // Extraer relaciones
        val familiasNombres = mapa["familias"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val categoriasNombres = mapa["categorias"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val subcategoriasNombres = mapa["subcategorias"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

        // Extraer atributos (formato: "clave1:valor1,clave2:valor2")
        val atributos = mapa["atributos"]?.split(",")?.mapNotNull { atributo ->
            val partes = atributo.split(":")
            if (partes.size == 2) {
                partes[0].trim() to partes[1].trim()
            } else null
        }?.toMap() ?: emptyMap()

        return ProductoImportExtendido(
            productoBase = productoBase,
            familiasNombres = familiasNombres,
            categoriasNombres = categoriasNombres,
            subcategoriasNombres = subcategoriasNombres,
            atributos = atributos
        )
    }
    private fun parsearLineaCsv(linea: String): List<String> {
        val resultado = mutableListOf<String>()
        val sb = StringBuilder()
        var dentroDeComillas = false
        var i = 0

        while (i < linea.length) {
            val char = linea[i]

            when {
                char == '"' -> {
                    if (dentroDeComillas && i + 1 < linea.length && linea[i + 1] == '"') {
                        // Doble comilla escapada
                        sb.append('"')
                        i++ // Saltar la segunda comilla
                    } else {
                        // Cambiar estado de comillas
                        dentroDeComillas = !dentroDeComillas
                    }
                }
                char == ',' && !dentroDeComillas -> {
                    // Separador encontrado fuera de comillas
                    resultado.add(sb.toString())
                    sb.clear()
                }
                else -> {
                    sb.append(char)
                }
            }
            i++
        }

        // Agregar último valor
        resultado.add(sb.toString())

        return resultado
    }

    private fun parsearProductoDesdeLinea(
        headers: List<String>,
        valores: List<String>,
        numeroFila: Int
    ): ProductoImport {
        if (headers.size != valores.size) {
            throw Exception("Número de columnas no coincide con headers (${headers.size} vs ${valores.size})")
        }

        val mapa = headers.zip(valores).toMap()

        return ProductoImport(
            numeroFila = numeroFila,
            id = mapa["id"]?.toLongOrNull() ?: 0L,
            nombre = mapa["nombre"]?.trim()
                ?: throw Exception("Nombre es obligatorio"),
            descripcion = mapa["descripcion"]?.trim()?.takeIf { it.isNotEmpty() },
            skuInterno = mapa["skuinterno"]?.trim()
                ?: throw Exception("SKU interno es obligatorio"),
            codigoBarras = mapa["codigobarras"]?.trim()?.takeIf { it.isNotEmpty() },
            precioCompra = mapa["preciocompra"]?.toDoubleOrNull()
                ?: throw Exception("Precio de compra debe ser un número válido"),
            precioVenta = mapa["precioventa"]?.toDoubleOrNull()
                ?: throw Exception("Precio de venta debe ser un número válido"),
            cantidadActual = mapa["cantidadactual"]?.toIntOrNull() ?: 0,
            cantidadMinima = mapa["cantidadminima"]?.toIntOrNull() ?: 0,
            cantidadMaximaComprada = mapa["cantidadmaxima"]?.toIntOrNull() ?: 0,
            proveedorPrincipalId = mapa["proveedorprincipalid"]?.toLongOrNull(),
            color = mapa["color"]?.trim()?.takeIf { it.isNotEmpty() },
            fechaUltimaVenta = mapa["fechaultimaventa"]?.toLongOrNull(),
            fechaUltimaCompra = mapa["fechaultimacompra"]?.toLongOrNull(),
            activo = mapa["activo"]?.lowercase()?.let {
                it == "true" || it == "1" || it == "sí" || it == "si"
            } ?: true,
            erroresValidacion = mutableListOf()
        )
    }

    private fun validarFilaProducto(valores: List<String>, numeroFila: Int) {
        if (valores.size < HEADERS_ESPERADOS.size) {
            throw Exception("Faltan columnas (encontradas ${valores.size}, esperadas ${HEADERS_ESPERADOS.size})")
        }

        // Validaciones básicas
        if (valores[1].trim().isEmpty()) { // nombre
            throw Exception("Nombre no puede estar vacío")
        }

        if (valores[3].trim().isEmpty()) { // skuInterno
            throw Exception("SKU no puede estar vacío")
        }

        // Validar números
        valores[5].toDoubleOrNull() ?: throw Exception("Precio de compra inválido")
        valores[6].toDoubleOrNull() ?: throw Exception("Precio de venta inválido")
    }
}