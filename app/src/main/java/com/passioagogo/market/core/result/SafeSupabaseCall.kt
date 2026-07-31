package com.passioagogo.market.core.result

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException

/**
 * Ejecuta [block] y traduce las excepciones de supabase-kt/Ktor a [DataResult].
 *
 * Los triggers de negocio (08_business_triggers.sql) llegan como RestException
 * con el mensaje de `raise exception` — se exponen como [DataError.Business]
 * para mostrarlos al usuario tal cual.
 */
suspend fun <T> safeSupabaseCall(block: suspend () -> T): DataResult<T> = try {
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
    DataResult.Error(error)
} catch (e: HttpRequestTimeoutException) {
    DataResult.Error(DataError.Network)
} catch (e: HttpRequestException) {
    DataResult.Error(DataError.Network)
} catch (e: Exception) {
    DataResult.Error(DataError.Unknown(e.message))
}
