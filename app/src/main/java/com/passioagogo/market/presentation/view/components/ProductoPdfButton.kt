package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProductoPdfButton(
    productoId: Long,
    nombreProducto: String,
    viewModel: CatalogoPdfViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val pdfState by viewModel.pdfState.collectAsState()

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("📄 Generar Ficha Técnica PDF")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.resetState()
            },
            title = { Text("Ficha Técnica") },
            text = {
                when (val state = pdfState) {
                    is PdfGenerationState.Idle -> {
                        Text("¿Deseas generar la ficha técnica de este producto en PDF?")
                    }
                    is PdfGenerationState.Generating -> {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Generando PDF...")
                        }
                    }
                    is PdfGenerationState.Success -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("¡PDF generado exitosamente!")

                            Button(
                                onClick = { viewModel.shareWhatsApp(state.file) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Compartir por WhatsApp")
                            }

                            OutlinedButton(
                                onClick = { viewModel.shareGeneric(state.file) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Compartir por otras apps")
                            }
                        }
                    }
                    is PdfGenerationState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                if (pdfState is PdfGenerationState.Idle) {
                    Button(
                        onClick = {
                            viewModel.generateFichaProducto(productoId, nombreProducto)
                        }
                    ) {
                        Text("Generar")
                    }
                }
            },
            dismissButton = {
                if (pdfState !is PdfGenerationState.Generating) {
                    TextButton(
                        onClick = {
                            showDialog = false
                            viewModel.resetState()
                        }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }
}