package com.passioagogo.market.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageFileManager (private val context: Context) {

    private val imageDirectory: File by lazy {
        File(context.getExternalFilesDir(null), "product_images").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun saveImageFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo"))

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_${timestamp}_${System.currentTimeMillis()}.jpg"
            val file = File(imageDirectory, fileName)

            // Leer y procesar la imagen
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Redimensionar si es necesario
            val processedBitmap = resizeBitmapIfNeeded(bitmap)

            // Guardar imagen procesada
            FileOutputStream(file).use { output ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            }

            processedBitmap.recycle()
            if (processedBitmap != bitmap) bitmap.recycle()

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxDimension = 5000
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun deleteImage(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getImageFile(path: String): File? {
        return File(path).takeIf { it.exists() }
    }
}