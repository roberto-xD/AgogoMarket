package com.passioagogo.market.data.catalog

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.storage.Storage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface ProductImageRepository {
    /** Sube la imagen y devuelve su URL pública. */
    suspend fun upload(productId: String, jpegBytes: ByteArray): DataResult<String>

    /** Borra el objeto de Storage a partir de su URL pública (best-effort). */
    suspend fun delete(publicUrl: String): DataResult<Unit>
}

@Singleton
class ProductImageRepositoryImpl @Inject constructor(
    private val storage: Storage,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ProductImageRepository {

    private companion object { const val BUCKET = "productos" }

    override suspend fun upload(productId: String, jpegBytes: ByteArray): DataResult<String> =
        withContext(io) {
            safeSupabaseCall {
                val path = "$productId/${UUID.randomUUID()}.jpg"
                storage.from(BUCKET).upload(path, jpegBytes)
                storage.from(BUCKET).publicUrl(path)
            }
        }

    override suspend fun delete(publicUrl: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            // .../storage/v1/object/public/productos/{path}
            val path = publicUrl.substringAfter("/$BUCKET/", missingDelimiterValue = "")
            if (path.isNotBlank()) {
                storage.from(BUCKET).delete(path)
            }
        }
    }
}
