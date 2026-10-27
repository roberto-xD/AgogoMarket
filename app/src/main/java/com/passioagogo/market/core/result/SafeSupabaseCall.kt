package com.passioagogo.market.core.result

import android.util.Log
import com.passioagogo.market.BuildConfig
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException

/** Etiqueta única para filtrar en Logcat: `adb logcat -s PG`. */
private const val TAG = "PG"

/**
 * Ejecuta [block] y traduce las excepciones de supabase-kt/Ktor a [DataResult].
 *
 * Los triggers de negocio (08_business_triggers.sql) llegan como RestException
 * con el mensaje de `raise exception` — se exponen como [DataError.Business]
 * para mostrarlos al usuario tal cual.
 *
 * **Registro.** Todo fallo se escribe en Logcat bajo la etiqueta `PG`. Este
 * es el único punto por el que pasan las llamadas a Supabase, así que
 * instrumentarlo aquí cubre los 23 repositorios sin repetir una línea de
 * log en cada método.
 *
 * @param operacion nombre legible de la operación. Si se omite, se deduce
 *   de la pila de llamadas; pasarlo explícitamente da un mensaje más claro
 *   cuando la deducción se queda en una lambda anónima.
 */
suspend fun <T> safeSupabaseCall(
    operacion: String? = null,
    block: suspend () -> T,
): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: RestException) {
    val error = when (e.statusCode) {
        401 -> DataError.Unauthorized
        403 -> DataError.Forbidden
        404 -> DataError.NotFound
        else -> {
            // PostgREST envuelve los `raise exception` de los triggers en el body
            val message = e.error.ifBlank { e.message ?: "" }
            if (message.isNotBlank()) DataError.Business(message)
            else DataError.Unknown(null)
        }
    }
    registrar(operacion, "HTTP ${e.statusCode}", e.error.ifBlank { e.message }, e)
    DataResult.Error(error)
} catch (e: HttpRequestTimeoutException) {
    registrar(operacion, "Timeout", "La petición excedió el tiempo de espera", e)
    DataResult.Error(DataError.Network)
} catch (e: HttpRequestException) {
    registrar(operacion, "Red", e.message, e)
    DataResult.Error(DataError.Network)
} catch (e: Exception) {
    registrar(operacion, "Inesperado", e.message, e)
    DataResult.Error(DataError.Unknown(e.message))
}

/**
 * Escribe el fallo en Logcat.
 *
 * El mensaje del servidor solo se incluye en builds de depuración: un
 * `raise exception` puede arrastrar valores de la fila implicada, y los
 * logs de un APK de producción son legibles por otras apps del sistema
 * en algunos dispositivos.
 */
private fun registrar(
    operacion: String?,
    tipo: String,
    detalle: String?,
    e: Throwable,
) {
    val donde = operacion ?: deducirOrigen()
    val mensaje = buildString {
        append("[$tipo] $donde")
        if (BuildConfig.DEBUG && !detalle.isNullOrBlank()) {
            append(" :: $detalle")
        }
    }
    Log.i(TAG, mensaje)
    // La traza completa solo en depuración: en producción basta la línea
    if (BuildConfig.DEBUG) Log.i(TAG, Log.getStackTraceString(e))
}

/**
 * Busca en la pila quién llamó, para no tener que etiquetar cada llamada.
 *
 * Solo se ejecuta en la rama de error, así que no cuesta nada en el camino
 * normal. Se salta las clases de infraestructura hasta dar con la primera
 * del proyecto, que es el repositorio que originó la consulta.
 */
private fun deducirOrigen(): String {
    val pila = Throwable().stackTrace
    val marco = pila.firstOrNull { marco ->
        marco.className.startsWith("com.passioagogo.market") &&
            !marco.className.contains("SafeSupabaseCall") &&
            !marco.className.contains("DataResult")
    } ?: run {
        // Sin marco propio en la pila: la llamada se reanudó en un hilo
        // del cliente HTTP y el repositorio ya no aparece. Se devuelve la
        // primera clase ajena útil en vez de dejarlo en blanco.
        val ajeno = pila.firstOrNull {
            !it.className.startsWith("kotlin") && !it.className.startsWith("java")
        }
        return ajeno?.let { "${it.className.substringAfterLast('.')}.${it.methodName}" }
            ?: "origen desconocido"
    }

    val clase = marco.className
        .substringAfterLast('.')
        // Las lambdas suspend generan clases sintéticas tipo Repo$metodo$1
        .substringBefore('$')
    return "$clase.${marco.methodName}"
}
