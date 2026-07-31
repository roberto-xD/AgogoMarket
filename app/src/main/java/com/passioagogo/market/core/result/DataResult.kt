package com.passioagogo.market.core.result

/**
 * Resultado de operaciones de datos. Mismo patrón que el cliente web KMP
 * para mantener consistencia entre plataformas.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val error: DataError) : DataResult<Nothing>
}

sealed interface DataError {
    /** Sin conexión o timeout */
    data object Network : DataError

    /** Sesión inválida o expirada */
    data object Unauthorized : DataError

    /** RLS negó la operación (rol sin permiso) */
    data object Forbidden : DataError

    data object NotFound : DataError

    /** Regla de negocio del servidor (triggers): stock insuficiente,
     *  transición de estado inválida, promo traslapada, etc. */
    data class Business(val message: String) : DataError

    data class Unknown(val message: String?) : DataError
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Error -> this
}

inline fun <T> DataResult<T>.onSuccess(block: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) block(data)
    return this
}

inline fun <T> DataResult<T>.onError(block: (DataError) -> Unit): DataResult<T> {
    if (this is DataResult.Error) block(error)
    return this
}
