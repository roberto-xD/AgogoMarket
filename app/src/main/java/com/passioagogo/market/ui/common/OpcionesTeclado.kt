package com.passioagogo.market.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/**
 * Opciones de teclado para los campos de texto de la app.
 *
 * Se centralizan aquí para que la capitalización sea coherente: antes
 * cada campo heredaba el valor por omisión de Compose, que no capitaliza
 * nada, y los nombres de producto o cliente acababan en minúscula según
 * el teclado de cada quien.
 *
 * El criterio: la primera letra de la frase en lo que es texto redactado,
 * ninguna en lo que es un identificador o un dato técnico.
 */
object OpcionesTeclado {

    /** Nombres, títulos, notas: lo que se lee como una frase. */
    val Texto = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
    )

    /** Igual, pero avanzando al siguiente campo del formulario. */
    val TextoSiguiente = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next,
    )

    /** Párrafos largos: descripciones, detalles, cuerpos de texto. */
    val Parrafo = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
    )

    /**
     * SKU, códigos y claves: NO se capitalizan.
     * Un SKU que cambia de caja puede dejar de coincidir al buscarlo.
     */
    val Codigo = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
    )

    /** Correos: nunca capitalizan, y el teclado muestra la arroba. */
    val Correo = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Email,
    )

    val Numero = KeyboardOptions(keyboardType = KeyboardType.Number)

    val Decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    val Telefono = KeyboardOptions(keyboardType = KeyboardType.Phone)

    val Url = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Uri,
    )
}
