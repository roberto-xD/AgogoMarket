package com.passioagogo.market.presentation.view.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.passioagogo.market.domain.bean.EstructuraValidacionCompleta

@Composable
fun EstructuraValidacionCard(validacion: EstructuraValidacionCompleta) {
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

            // Información básica
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Filas", validacion.totalFilas.toString())
                InfoItem("Headers", validacion.headersEncontrados.size.toString())
                if (validacion.tieneDatosMaestros) {
                    InfoItem("Datos maestros", "✅")
                }
                if (validacion.tieneRelaciones) {
                    InfoItem("Relaciones", "✅")
                }
            }

            // Detalles de datos maestros si existen
            if (validacion.tieneDatosMaestros) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📋 Datos Maestros Detectados:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        validacion.estructuraFamilias?.let {
                            Text("• Familias: ${if (it.esValida) "✅" else "❌"}")
                        }

                        validacion.estructuraCategorias?.let {
                            Text("• Categorías: ${if (it.esValida) "✅" else "❌"}")
                        }

                        validacion.estructuraSubcategorias?.let {
                            Text("• Subcategorías: ${if (it.esValida) "✅" else "❌"}")
                        }
                    }
                }
            }

            // Relaciones detectadas
            if (validacion.tieneRelaciones) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🔗 Relaciones Detectadas:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (validacion.relacionesDetectadas.familias) {
                            Text("• Productos → Familias")
                        }
                        if (validacion.relacionesDetectadas.categorias) {
                            Text("• Productos → Categorías")
                        }
                        if (validacion.relacionesDetectadas.subcategorias) {
                            Text("• Productos → Subcategorías")
                        }
                    }
                }
            }

            // Errores si existen
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
        }
    }
}