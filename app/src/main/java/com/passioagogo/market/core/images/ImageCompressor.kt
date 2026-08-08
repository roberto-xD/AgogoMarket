package com.passioagogo.market.core.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Redimensiona (lado mayor <= [MAX_DIM] px), corrige orientacion y comprime
 * a JPEG antes de subir a Storage.
 *
 * En API 28+ usa ImageDecoder: decodifica HEIC/HEIF (formato por defecto de
 * muchas camaras, que BitmapFactory no soporta) y aplica la orientacion EXIF
 * automaticamente. BitmapFactory queda solo como respaldo para API 26-27.
 *
 * Devuelve [Result] para que la UI pueda mostrar la causa real del fallo.
 */
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        const val MAX_DIM = 1280
        const val JPEG_QUALITY = 80
    }

    suspend fun compress(uri: Uri): Result<ByteArray> = withContext(Dispatchers.Default) {
        runCatching {
            val bitmap = decode(uri)
            try {
                ByteArrayOutputStream().use { out ->
                    val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                    check(ok) { "El sistema no pudo comprimir la imagen a JPEG" }
                    out.toByteArray()
                }
            } finally {
                bitmap.recycle()
            }
        }
    }

    private fun decode(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) decodeModern(uri)
        else decodeLegacy(uri)

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeModern(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            // Software: los bitmaps de hardware no se pueden comprimir
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
            val width = info.size.width
            val height = info.size.height
            val maxSide = maxOf(width, height)
            if (maxSide > MAX_DIM) {
                val scale = MAX_DIM.toFloat() / maxSide
                decoder.setTargetSize(
                    (width * scale).toInt().coerceAtLeast(1),
                    (height * scale).toInt().coerceAtLeast(1),
                )
            }
        }
    }

    private fun decodeLegacy(uri: Uri): Bitmap {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: error("No se pudo abrir la imagen seleccionada")
        check(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "Formato de imagen no soportado en este dispositivo"
        }

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
        } ?: error("No se pudo decodificar la imagen")

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

        val rotation = runCatching {
            resolver.openInputStream(uri)?.use { stream ->
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
        }.getOrDefault(0f)

        if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        return bitmap
    }
}
