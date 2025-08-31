package com.passioagogo.market.domain.bean

data class EstructuraValidacionCompleta(
    val esValida: Boolean,
    val headersFaltantes: List<String>,
    val headersEncontrados: List<String>,
    val totalFilas: Int,
    val tieneDatosMaestros: Boolean,
    val estructuraFamilias: ValidacionEstructura?,
    val estructuraCategorias: ValidacionEstructura?,
    val estructuraSubcategorias: ValidacionEstructura?,
    val tieneRelaciones: Boolean,
    val relacionesDetectadas: RelacionesDetectadas
)

data class ValidacionEstructura(
    val esValida: Boolean,
    val headersFaltantes: List<String>,
    val headersEncontrados: List<String>
)

data class RelacionesDetectadas(
    val familias: Boolean,
    val categorias: Boolean,
    val subcategorias: Boolean
)