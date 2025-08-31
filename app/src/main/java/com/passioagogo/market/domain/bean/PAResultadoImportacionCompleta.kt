package com.passioagogo.market.domain.bean

data class ResultadoImportacionCompleta(
    // Resultados de productos (mantiene compatibilidad)
    val totalProcesados: Int,
    val exitosos: Int,
    val fallidos: Int,
    val errores: List<ErrorImportacion>,
    val duplicadosEncontrados: Int,
    val tiempoTranscurrido: Long,
    val resultadoFamilias: ResultadoImportacionItem,
    val resultadoCategorias: ResultadoImportacionItem,
    val resultadoSubcategorias: ResultadoImportacionItem,
    val relacionesCreadas: Int
)
