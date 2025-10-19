package com.passioagogo.market.domain.mapper

import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.dinamics.ProductoAtributoEntity
import com.passioagogo.market.domain.model.catalogo.AtributoCatalogo
import com.passioagogo.market.domain.model.catalogo.ProductoCatalogo
import com.passioagogo.market.domain.model.catalogo.TipoAtributo
import kotlin.collections.map

object CatalogoMapper {

    fun mapProductoToProductoCatalogo(
        producto: ProductoEntity,
        atributos: List<ProductoAtributoEntity>
    ): ProductoCatalogo {
        val imagenPrincipal = producto.imagenes
            .firstOrNull { it.esPrincipal }?.rutaImagen
            ?: producto.imagenes.firstOrNull()?.rutaImagen

        return ProductoCatalogo(
            id = producto.id,
            nombre = producto.nombre,
            descripcion = producto.descripcion,
            precio = producto.precioVenta,
            imagenPrincipal = imagenPrincipal,
            atributos = atributos.map { mapAtributo(it) },
            color = producto.color
        )
    }

    private fun mapAtributo(atributo: ProductoAtributoEntity): AtributoCatalogo {
        return AtributoCatalogo(
            nombre = atributo.tipoAtributo.nombre,
            valor = atributo.valor,
            tipo = mapTipoAtributo(atributo.tipoAtributo.tipoDato),
            icono = getIconoParaAtributo(atributo.tipoAtributo.nombre)
        )
    }

    private fun mapTipoAtributo(tipoDato: String): TipoAtributo {
        return when (tipoDato.uppercase()) {
            "TEXT" -> TipoAtributo.TEXTO
            "NUMBER" -> TipoAtributo.NUMERO
            "BOOLEAN" -> TipoAtributo.BOOLEAN
            "DATE" -> TipoAtributo.FECHA
            else -> TipoAtributo.TEXTO
        }
    }

    private fun getIconoParaAtributo(nombreAtributo: String): String? {
        // Mapeo de nombres de atributos a emojis/iconos
        return when (nombreAtributo.lowercase()) {
            "recargable" -> "🔄"
            "a prueba de salpicaduras" -> "💧"
            "silencioso" -> "🔇"
            "silicón quirúrgico", "silicon quirurgico" -> "✨"
            "10 frecuencias de vibración", "frecuencias" -> "📳"
            "regulación de succión" -> "⚙️"
            else -> null
        }
    }
}