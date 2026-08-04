package com.passioagogo.market.core.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Redimensiona (lado mayor ≤ [MAX_DIM] px), corrige la orientación EXIF
 * y comprime a JPEG antes de subir a Storage. Una foto de cámara de
 * ~8 MB queda en ~150-300 KB, apta para listas del catálogo.
 */
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        const val MAX_DIM = 1280
        const val JPEG_QUALITY = 80
    }

    suspend fun compress(uri: Uri): ByteArray? = withContext(Dispatchers.Default) {
        val resolver = context.contentResolver

        // 1) Dimensiones sin decodificar
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: return@withContext null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        // 2) Decodificar con sampleo (potencias de 2, sin pasar de MAX_DIM)
        var sample = 1
        while (
            bounds.outWidth / (sample * 2) >= MAX_DIM ||
            bounds.outHeight / (sample * 2) >= MAX_DIM
        ) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return@withContext null

        // 3) Ajuste fino al límite exacto
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide > MAX_DIM) {
            val scale = MAX_DIM.toFloat() / maxSide
            bitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        }

        // 4) Orientación EXIF (fotos de galería suelen venir rotadas)
        val rotation = resolver.openInputStream(uri)?.use { stream ->
            when (
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        // 5) JPEG comprimido
        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
    }
}
