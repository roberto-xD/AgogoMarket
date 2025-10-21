package com.passioagogo.market.pdf.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.passioagogo.market.domain.model.catalogo.CatalogoCompleto
import com.passioagogo.market.domain.model.catalogo.ProductoCatalogo
import com.passioagogo.market.pdf.template.CatalogoHtmlTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun generateCatalogoCompleto(
        catalogo: CatalogoCompleto,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val writer = PdfWriter(outputFile)
            val pdfDoc = PdfDocument(writer)
            val converterProperties = setupConverterProperties()

            // Generar portada
            val portadaHtml = processHtmlWithImages(
                CatalogoHtmlTemplate.generatePortada(catalogo)
            )
            HtmlConverter.convertToPdf(portadaHtml, pdfDoc, converterProperties)

            // Generar páginas por familia y categoría
            catalogo.familias.forEach { familia ->
                familia.categorias.forEach { categoria ->
                    if (categoria.productos.isNotEmpty()) {
                        val paginaHtml = processHtmlWithImages(
                            CatalogoHtmlTemplate.generateProductosPage(categoria, categoria.productos)
                        )
                        HtmlConverter.convertToPdf(paginaHtml, pdfDoc, converterProperties)
                    }
                }
            }

            pdfDoc.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun generateFichaProducto(
        producto: ProductoCatalogo,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val writer = PdfWriter(outputFile)
            val pdfDoc = PdfDocument(writer)
            val converterProperties = setupConverterProperties()

            val fichaHtml = processHtmlWithImages(
                CatalogoHtmlTemplate.generateFichaProducto(producto)
            )

            HtmlConverter.convertToPdf(fichaHtml, pdfDoc, converterProperties)
            pdfDoc.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun setupConverterProperties(): ConverterProperties {
        val converterProperties = ConverterProperties()
        val fontProvider = DefaultFontProvider(true, true, true)
        converterProperties.fontProvider = fontProvider
        converterProperties.baseUri = context.filesDir.absolutePath
        return converterProperties
    }

    private suspend fun processHtmlWithImages(html: String): String = withContext(Dispatchers.IO) {
        var processedHtml = html

        // Buscar todas las rutas de imágenes locales y convertirlas a base64
        val imagePattern = """src="([^"]+)"""".toRegex()
        imagePattern.findAll(html).forEach { match ->
            val imagePath = match.groupValues[1]
            if (!imagePath.startsWith("http") && !imagePath.startsWith("data:")) {
                try {
                    val base64Image = convertLocalImageToBase64(imagePath)
                    processedHtml = processedHtml.replace(
                        """src="$imagePath"""",
                        """src="data:image/jpeg;base64,$base64Image""""
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        processedHtml
    }

    private fun convertLocalImageToBase64(imagePath: String): String {
        val bitmap = loadBitmapFromPath(imagePath)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
    }

    private fun loadBitmapFromPath(path: String): Bitmap {
        return try {
            // Intentar cargar desde File
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                // Intentar cargar desde drawable por nombre
                val resourceId = context.resources.getIdentifier(
                    path.substringBeforeLast("."),
                    "drawable",
                    context.packageName
                )
                BitmapFactory.decodeResource(context.resources, resourceId)
            }
        } catch (e: Exception) {
            // Crear bitmap placeholder
            Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        }
    }
}