package com.passioagogo.market.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val context: Context,
    private val storage: Storage
) {
    private val bucketName = "product-images"

    suspend fun uploadImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> {
        return try {
            storage.from(bucketName).upload(path = fileName, data = imageBytes) { upsert = true }
            val publicUrl = storage.from(bucketName).publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImageFromUri(
        fileName: String,
        uri: Uri
    ): Result<String> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.failure(Exception("No se pudo leer la imagen"))

            uploadImage(fileName, bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImage(fileName: String): Result<Unit> {
        return try {
            storage.from(bucketName).delete(fileName)
            Log.i(TAG_PG,"borrado exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.i(TAG_PG,"borrado fallido: $e")
            Result.failure(e)
        }
    }

    fun getPublicUrl(fileName: String): String {
        return storage.from(bucketName).publicUrl(fileName)
    }
}