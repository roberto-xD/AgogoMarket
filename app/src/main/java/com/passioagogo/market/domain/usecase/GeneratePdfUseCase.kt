package com.passioagogo.market.domain.usecase

import com.passioagogo.market.domain.repository.CatalogoRepository
import com.passioagogo.market.pdf.generator.PdfGenerator
import java.io.File
import javax.inject.Inject

class GeneratePdfUseCase @Inject constructor(
    private val catalogoRepository: CatalogoRepository,
    private val pdfGenerator: PdfGenerator
) {

    suspend fun generateCatalogoCompleto(outputFile: File): Result<File> {
        return try {
            val catalogo = catalogoRepository.getCatalogoCompleto()
            pdfGenerator.generateCatalogoCompleto(catalogo, outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateFichaProducto(productoId: Long, outputFile: File): Result<File> {
        return try {
            val producto = catalogoRepository.getProductoParaCatalogo(productoId)
                ?: return Result.failure(Exception("Producto no encontrado"))

            pdfGenerator.generateFichaProducto(producto, outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}