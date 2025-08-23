package com.passioagogo.market.presentation.view.components.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passioagogo.market.domain.bean.EstructuraValidacion

@Composable
fun EstructuraValidacionCard(validacion: EstructuraValidacion) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "2. Validación de Estructura",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Estado general
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (validacion.esValida) Icons.Default.CheckCircle else Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = if (validacion.esValida) Color.Green else Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (validacion.esValida) "Estructura válida" else "Estructura inválida",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (validacion.esValida) Color.Green else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detalles
            Text("Total de filas: ${validacion.totalFilas}")
            Text("Headers encontrados: ${validacion.headersEncontrados.size}")

            if (validacion.headersFaltantes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "❌ Headers faltantes:",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                validacion.headersFaltantes.forEach { header ->
                    Text("• $header", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (validacion.erroresEstructura.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "❌ Errores de estructura:",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                validacion.erroresEstructura.forEach { error ->
                    Text("• $error", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}