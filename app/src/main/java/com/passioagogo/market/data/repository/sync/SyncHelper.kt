package com.passioagogo.market.data.repository.sync

import android.util.Log
import kotlinx.coroutines.delay

object SyncHelper {
    private const val TAG = "SyncHelper"

    /**
     * Ejecuta una operación de sincronización con reintentos
     */
    suspend fun <T> withRetry(
        maxRetries: Int = 3,
        delayMs: Long = 1000,
        operation: suspend () -> T
    ): Result<T> {
        repeat(maxRetries) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                Log.e(TAG, "Intento ${attempt + 1} falló: ${e.message}")
                if (attempt < maxRetries - 1) {
                    delay(delayMs * (attempt + 1))
                } else {
                    return Result.failure(e)
                }
            }
        }
        return Result.failure(Exception("No se pudo completar la operación después de $maxRetries intentos"))
    }

    /**
     * Validar que el userId esté presente
     */
    fun requireUserId(userId: String?): String {
        return userId ?: throw IllegalStateException("UserId no puede ser null para sincronización")
    }
}

/**
 * Estrategia de resolución de conflictos
 */
enum class ConflictStrategy {
    LOCAL_WINS,      // Mantener cambios locales
    REMOTE_WINS,     // Usar cambios remotos
    NEWEST_WINS,     // El más reciente gana (por timestamp)
    MERGE            // Intentar fusionar cambios
}

/**
 * Resolver conflictos entre datos locales y remotos
 */
object ConflictResolver {
    private const val TAG = "ConflictResolver"

    fun <T> resolveConflict(
        local: T,
        remote: T,
        strategy: ConflictStrategy = ConflictStrategy.NEWEST_WINS,
        localTimestamp: Long? = null,
        remoteTimestamp: Long? = null
    ): T {
        return when (strategy) {
            ConflictStrategy.LOCAL_WINS -> {
                Log.d(TAG, "Conflicto resuelto: LOCAL_WINS")
                local
            }
            ConflictStrategy.REMOTE_WINS -> {
                Log.d(TAG, "Conflicto resuelto: REMOTE_WINS")
                remote
            }
            ConflictStrategy.NEWEST_WINS -> {
                if (localTimestamp != null && remoteTimestamp != null) {
                    if (localTimestamp > remoteTimestamp) {
                        Log.d(TAG, "Conflicto resuelto: LOCAL es más reciente")
                        local
                    } else {
                        Log.d(TAG, "Conflicto resuelto: REMOTE es más reciente")
                        remote
                    }
                } else {
                    Log.d(TAG, "Conflicto resuelto: REMOTE_WINS (sin timestamps)")
                    remote
                }
            }
            ConflictStrategy.MERGE -> {
                Log.w(TAG, "MERGE no implementado, usando REMOTE_WINS")
                remote
            }
        }
    }
}