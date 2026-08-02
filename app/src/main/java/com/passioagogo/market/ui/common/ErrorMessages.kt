package com.passioagogo.market.ui.common

import com.passioagogo.market.core.result.DataError

/** Traducción de errores de datos a mensajes para el usuario. */
fun DataError.toMessage(): String = when (this) {
    DataError.Network -> "Sin conexión. Revisa tu internet e intenta de nuevo."
    DataError.Unauthorized -> "Tu sesión expiró. Vuelve a iniciar sesión."
    DataError.Forbidden -> "No tienes permiso para esta operación."
    DataError.NotFound -> "No se encontró el registro."
    is DataError.Business -> message
    is DataError.Unknown -> "Ocurrió un error inesperado." +
        (message?.let { " ($it)" } ?: "")
}
