package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CatalogoPdfScreen(
    viewModel: CatalogoPdfViewModel = hiltViewModel()
) {
    val pdfState by viewModel.pdfState.collectAsState()

    LaunchedEffect(pdfState) {
        if (pdfState is PdfGenerationState.Success) {
            // Opcional: mostrar notificación de éxito
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Generar Catálogo PDF",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (val state = pdfState) {
            is PdfGenerationState.Idle -> {
                IdleContent(
                    onGenerateCatalogo = { viewModel.generateCatalogoCompleto() }
                )
            }

            is PdfGenerationState.Generating -> {
                GeneratingContent()
            }

            is PdfGenerationState.Success -> {
                SuccessContent(
                    onShareWhatsApp = { viewModel.shareWhatsApp(state.file) },
                    onShareOther = { viewModel.shareGeneric(state.file) },
                    onSaveLocal = {
                        val saved = viewModel.savePdfToDownloads(state.file)
                        // Mostrar mensaje según resultado
                    },
                    onReset = { viewModel.resetState() }
                )
            }

            is PdfGenerationState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.generateCatalogoCompleto() }
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    onGenerateCatalogo: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onGenerateCatalogo,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Generar Catálogo Completo")
        }

        Text(
            text = "El catálogo incluirá todos los productos organizados por categorías",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GeneratingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Generando PDF...",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Esto puede tomar unos momentos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessContent(
    onShareWhatsApp: () -> Unit,
    onShareOther: () -> Unit,
    onSaveLocal: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "¡PDF Generado!",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onShareWhatsApp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("📱 Compartir por WhatsApp")
        }

        OutlinedButton(
            onClick = onShareOther,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("📤 Compartir por otras apps")
        }

        OutlinedButton(
            onClick = onSaveLocal,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("💾 Guardar en Descargas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onReset) {
            Text("Generar otro PDF")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Error al generar PDF",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Reintentar")
        }
    }
}