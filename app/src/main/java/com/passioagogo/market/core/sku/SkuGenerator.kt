package com.passioagogo.market.core.sku

import java.text.Normalizer

/**
 * Genera SKU internos con la forma PREFIJO-SUFIJO (p. ej. "WHE-4K7M").
 *
 * · Prefijo: hasta 3 letras derivadas del nombre del producto. Solo es una
 *   pista visual para reconocerlo en listas; el identificador real es el
 *   conjunto, así que renombrar el producto NO obliga a cambiar el SKU.
 * · Sufijo: 4 caracteres de un alfabeto sin ambigüedades (sin 0/O ni 1/I/L),
 *   porque estos códigos acaban dictándose por teléfono y tecleándose a mano.
 *
 * 31^4 ≈ 923k combinaciones por prefijo: las colisiones son improbables, y de
 * todos modos se comprueban antes de asignar y la columna `sku` es UNIQUE.
 *
 * Apto para imprimir en Code 128, que admite alfanuméricos y no requiere
 * registro GS1 al ser de uso interno.
 */
object SkuGenerator {

    private const val ALFABETO = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
    private const val LARGO_SUFIJO = 4

    fun generate(nombreProducto: String): String =
        "${prefijo(nombreProducto)}-${sufijo()}"

    private fun sufijo(): String =
        (1..LARGO_SUFIJO).map { ALFABETO.random() }.joinToString("")

    private fun prefijo(nombre: String): String {
        val palabras = Normalizer.normalize(nombre, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")       // quita acentos
            .uppercase()
            .split(Regex("[^A-Z0-9]+"))
            .filter { it.isNotBlank() }

        return when {
            palabras.isEmpty() -> "PAG"
            // Tres o más palabras: iniciales ("Copa Mastrubadora Realista" → CMR)
            palabras.size >= 3 -> palabras.take(3).map { it.first() }.joinToString("")
            // Dos palabras: dos letras de la primera + inicial de la segunda
            palabras.size == 2 ->
                (palabras[0].take(2) + palabras[1].first()).padEnd(3, 'X')
            // Una palabra: sus tres primeras letras
            else -> palabras[0].take(3).padEnd(3, 'X')
        }
    }
}
