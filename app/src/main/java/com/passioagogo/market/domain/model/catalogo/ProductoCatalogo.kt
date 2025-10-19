package com.passioagogo.market.domain.model.catalogo

data class ProductoCatalogo(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val imagenPrincipal: String?, // ruta local
    val atributos: List<AtributoCatalogo>,
    val color: String?
)

data class AtributoCatalogo(
    val nombre: String, // "Recargable", "Silicón quirúrgico", etc.
    val valor: String, // "Sí", "10 frecuencias", etc.
    val tipo: TipoAtributo,
    val icono: String? = null // nombre del drawable si existe
)

enum class TipoAtributo {
    TEXTO, NUMERO, BOOLEAN, FECHA
}

data class CategoriaCatalogo(
    val nombre: String,
    val icono: String?, // emoji o drawable
    val productos: List<ProductoCatalogo>
)

data class FamiliaCatalogo(
    val nombre: String,
    val categorias: List<CategoriaCatalogo>
)

data class CatalogoCompleto(
    val titulo: String = "CATÁLOGO 2025",
    val subtitulo: String = "TIENDA PARA ADULTOS",
    val nombreNegocio: String = "Passion à gogo",
    val logo: String?, // ruta del logo
    val familias: List<FamiliaCatalogo>,
    val colorPrimario: String = "#7B2CBF", // morado del diseño
    val colorSecundario: String = "#FFFFFF"
)