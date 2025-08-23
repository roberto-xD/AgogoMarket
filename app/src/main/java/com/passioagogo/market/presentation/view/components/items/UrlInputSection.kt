package com.passioagogo.market.presentation.view.components.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UrlInputSection(
    url: String,
    onUrlChange: (String) -> Unit,
    validandoUrl: Boolean,
    urlValida: Boolean,
    mensajeValidacion: String?
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "1. URL del Google Spreadsheet",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("URL del documento") },
                placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                trailingIcon = {
                    when {
                        validandoUrl -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        urlValida -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                        url.isNotBlank() && !urlValida -> Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color.Red)
                        else -> null
                    }
                },
                supportingText = {
                    Column {
                        Text("Pegue el enlace completo del Google Spreadsheet")
                        mensajeValidacion?.let { mensaje ->
                            Text(
                                text = mensaje,
                                color = if (urlValida) Color.Green else Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instrucciones
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📋 Instrucciones:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Haga el documento público o compartible\n" +
                                "• Asegúrese que la primera fila contenga los encabezados\n" +
                                "• Use el enlace de 'Compartir' del documento",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}