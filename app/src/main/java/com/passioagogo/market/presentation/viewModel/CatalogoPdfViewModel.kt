package com.passioagogo.market.presentation.viewModel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.usecase.GeneratePdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class PdfGenerationState {
    object Idle : PdfGenerationState()
    object Generating : PdfGenerationState()
    data class Success(val file: File) : PdfGenerationState()
    data class Error(val message: String) : PdfGenerationState()
}

@HiltViewModel
class CatalogoPdfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generatePdfUseCase: GeneratePdfUseCase
) : ViewModel() {

    private val _pdfState = MutableStateFlow<PdfGenerationState>(PdfGenerationState.Idle)
    val pdfState: StateFlow<PdfGenerationState> = _pdfState.asStateFlow()

    fun generateCatalogoCompleto() {
        viewModelScope.launch {
            _pdfState.value = PdfGenerationState.Generating

            val outputFile = createPdfFile("Catalogo_Passion_Agogo_2025.pdf")

            generatePdfUseCase.generateCatalogoCompleto(outputFile)
                .onSuccess { file ->
                    _pdfState.value = PdfGenerationState.Success(file)
                }
                .onFailure { error ->
                    _pdfState.value = PdfGenerationState.Error(
                        error.message ?: "Error al generar el catálogo"
                    )
                }
        }
    }

    fun generateFichaProducto(productoId: Long, nombreProducto: String) {
        viewModelScope.launch {
            _pdfState.value = PdfGenerationState.Generating

            val fileName = "Ficha_${nombreProducto.replace(" ", "_")}.pdf"
            val outputFile = createPdfFile(fileName)

            generatePdfUseCase.generateFichaProducto(productoId, outputFile)
                .onSuccess { file ->
                    _pdfState.value = PdfGenerationState.Success(file)
                }
                .onFailure { error ->
                    _pdfState.value = PdfGenerationState.Error(
                        error.message ?: "Error al generar la ficha"
                    )
                }
        }
    }

    fun shareWhatsApp(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Verificar si WhatsApp está instalado
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // WhatsApp no instalado, intentar con WhatsApp Business
                intent.setPackage("com.whatsapp.w4b")
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    // Ninguna app de WhatsApp, mostrar selector genérico
                    shareGeneric(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _pdfState.value = PdfGenerationState.Error("Error al compartir por WhatsApp")
        }
    }

    fun shareGeneric(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Catálogo Passion à gogo")
                putExtra(Intent.EXTRA_TEXT, "Te comparto nuestro catálogo de productos")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Compartir catálogo"))
        } catch (e: Exception) {
            e.printStackTrace()
            _pdfState.value = PdfGenerationState.Error("Error al compartir el PDF")
        }
    }

    fun savePdfToDownloads(file: File): Boolean {
        return try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val destinationFile = File(downloadsDir, file.name)
            file.copyTo(destinationFile, overwrite = true)

            // Notificar al sistema que hay un nuevo archivo
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(destinationFile)
            context.sendBroadcast(intent)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetState() {
        _pdfState.value = PdfGenerationState.Idle
    }

    private fun createPdfFile(fileName: String): File {
        val pdfDir = File(context.filesDir, "pdfs")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        return File(pdfDir, fileName)
    }
}