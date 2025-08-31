package com.passioagogo.market.presentation.view.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passioagogo.market.domain.bean.ResultadoImportacionCompleta
import com.passioagogo.market.domain.bean.ResultadoImportacionProductos

@Composable
fun ResultadoImportacionCard(resultado : ResultadoImportacionCompleta) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Resultado de Importación",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Estadísticas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    valor = resultado.exitosos,
                    etiqueta = "Exitosos",
                    color = Color.Green
                )
                EstadisticaItem(
                    valor = resultado.fallidos,
                    etiqueta = "Fallidos",
                    color = Color.Red
                )
                EstadisticaItem(
                    valor = resultado.duplicadosEncontrados,
                    etiqueta = "Duplicados",
                    color = Color.Magenta
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información adicional
            Text("Total procesados: ${resultado.totalProcesados}")
            Text("Tiempo transcurrido: ${resultado.tiempoTranscurrido / 1000}s")

            // Errores si existen
            if (resultado.errores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "❌ Errores encontrados:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    val list = resultado.errores.take(10)
                    items(list.size) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "Fila ${list.get(index).numeroFila} - SKU: ${list.get(index).sku}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = list.get(index).mensaje,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    if (resultado.errores.size > 10) {
                        item {
                            Text(
                                text = "... y ${resultado.errores.size - 10} errores más",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}