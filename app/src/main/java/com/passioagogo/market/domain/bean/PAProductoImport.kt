package com.passioagogo.market.domain.bean

import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams

data class ProductoImport(
    val numeroFila: Int,
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val skuInterno: String,
    val codigoBarras: String?,
    val precioCompra: Double,
    val precioVenta: Double,
    val cantidadActual: Int,
    val cantidadMinima: Int,
    val cantidadMaximaComprada: Int,
    val proveedorPrincipalId: Long?,
    val color: String?,
    val fechaUltimaVenta: Long?,
    val fechaUltimaCompra: Long?,
    val activo: Boolean,
    val erroresValidacion: MutableList<String>
) {
    val esValido: Boolean
        get() = erroresValidacion.isEmpty()

    fun toCrearProductoParams(): GuardarProductoParams {
        return GuardarProductoParams(
            nombre = nombre,
            descripcion = descripcion,
            skuInterno = skuInterno,
            codigoBarras = codigoBarras,
            precioCompra = precioCompra,
            precioVenta = precioVenta,
            cantidadInicial = cantidadActual,
            cantidadMinima = cantidadMinima,
            proveedorPrincipalId = proveedorPrincipalId,
            color = color,
            categorias = emptyList(), // Se pueden añadir después
            subcategorias = emptyList(),
            atributos = emptyMap()
        )
    }
}

data class EstructuraValidacion(
    val esValida: Boolean,
    val headersFaltantes: List<String>,
    val headersEncontrados: List<String>,
    val erroresEstructura: List<String>,
    val totalFilas: Int
)

data class ResultadoImportacionProductos(
    val totalProcesados: Int,
    val exitosos: Int,
    val fallidos: Int,
    val errores: List<ErrorImportacion>,
    val duplicadosEncontrados: Int,
    val tiempoTranscurrido: Long,
)

data class ResultadoImportacionItem(
    val totalProcesados: Int,
    val exitosos: Int,
    val fallidos: Int,
    val errores: List<ErrorImportacion>
)

data class ErrorImportacion(
    val numeroFila: Int,
    val sku: String,
    val mensaje: String,
    val tipo: TipoErrorImportacion
)

enum class TipoErrorImportacion {
    SKU_DUPLICADO,
    CODIGO_BARRAS_DUPLICADO,
    DATOS_INVALIDOS,
    ERROR_BASE_DATOS,
    VALIDACION_NEGOCIO
}