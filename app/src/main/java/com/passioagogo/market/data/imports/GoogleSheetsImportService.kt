package com.passioagogo.market.data.imports

import com.google.gson.Gson
import com.passioagogo.market.domain.bean.EstructuraValidacion
import com.passioagogo.market.domain.bean.ProductoImport
import com.passioagogo.market.domain.state.PADomainState
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

interface GoogleSheetsImportService {
    suspend fun importarProductos(spreadsheetUrl: String): PADomainState<List<ProductoImport>>
    suspend fun validarEstructura(spreadsheetUrl: String): PADomainState<EstructuraValidacion>
    suspend fun obtenerVistaPrevia(spreadsheetUrl: String, limite: Int = 5): PADomainState<List<ProductoImport>>
}

@Singleton
class GoogleSheetsImportServiceImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) : GoogleSheetsImportService {

    companion object {
        // Encabezados esperados en el spreadsheet (orden importa)
        private val HEADERS_ESPERADOS = listOf(
            "id", "nombre", "descripcion", "skuInterno", "codigoBarras",
            "precioCompra", "precioVenta", "cantidadActual", "cantidadMinima",
            "cantidadMaximaComprada", "proveedorPrincipalId", "color",
            "fechaUltimaVenta", "fechaUltimaCompra", "activo"
        )
    }

    override suspend fun importarProductos(spreadsheetUrl: String): PADomainState<List<ProductoImport>> {
        return try {
            val csvUrl = convertirACsvUrl(spreadsheetUrl)
            val csvContent = descargarCsv(csvUrl)
            val productos = parsearCsv(csvContent)

            PADomainState.Success(productos)

        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun validarEstructura(spreadsheetUrl: String): PADomainState<EstructuraValidacion> {
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
            val headersFaltantes = HEADERS_ESPERADOS.filter { header ->
                !headersArchivo.contains(header.lowercase())
            }

            // Validar estructura de datos (primeras 5 filas)
            val erroresEstructura = mutableListOf<String>()
            val filasParaValidar = lineas.drop(1).take(5)

            filasParaValidar.forEachIndexed { index, linea ->
                try {
                    val valores = parsearLineaCsv(linea)
                    validarFilaProducto(valores, index + 2) // +2 porque empezamos en fila 2
                } catch (e: Exception) {
                    erroresEstructura.add("Fila ${index + 2}: ${e.message}")
                }
            }

            val validacion = EstructuraValidacion(
                esValida = headersFaltantes.isEmpty() && erroresEstructura.isEmpty(),
                headersFaltantes = headersFaltantes,
                headersEncontrados = headersArchivo,
                erroresEstructura = erroresEstructura,
                totalFilas = lineas.size - 1 // -1 por el header
            )

            PADomainState.Success(validacion)

        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun obtenerVistaPrevia(
        spreadsheetUrl: String,
        limite: Int
    ): PADomainState<List<ProductoImport>> {
        return try {
            val csvUrl = convertirACsvUrl(spreadsheetUrl)
            val csvContent = descargarCsv(csvUrl)
            val productos = parsearCsv(csvContent, limite)

            PADomainState.Success(productos)

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

    private fun parsearCsv(csvContent: String, limite: Int? = null): List<ProductoImport> {
        val lineas = csvContent.lines()

        if (lineas.size < 2) {
            throw Exception("El archivo debe contener al menos una fila de datos además del encabezado")
        }

        val headers = parsearLineaCsv(lineas.first()).map { it.trim().lowercase() }
        val filasData = if (limite != null) {
            lineas.drop(1).take(limite)
        } else {
            lineas.drop(1)
        }

        return filasData.mapIndexedNotNull { index, linea ->
            try {
                if (linea.trim().isEmpty()) return@mapIndexedNotNull null

                val valores = parsearLineaCsv(linea)
                parsearProductoDesdeLinea(headers, valores, index + 2)

            } catch (e: Exception) {
                throw Exception("Error en fila ${index + 2}: ${e.message}")
            }
        }
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