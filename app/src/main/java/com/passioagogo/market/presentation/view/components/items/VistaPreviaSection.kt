package com.passioagogo.market.presentation.view.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passioagogo.market.domain.bean.ProductoImport

@Composable
fun VistaPreviaSection(
    productos: List<ProductoImport>,
    cargando: Boolean,
    onCargarVistaPrevia: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4. Vista Previa",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(
                    onClick = onCargarVistaPrevia,
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Cargar Vista Previa")
                }
            }

            if (productos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Primeros ${productos.size} productos encontrados:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Tabla de vista previa
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text("SKU", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Nombre", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                            Text("Precio", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Stock", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Items
                    items(
                        count = productos.size,
                    ) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = productos.get(index).skuInterno,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = productos.get(index).nombre,
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${productos.get(index).precioVenta}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${productos.get(index).cantidadActual}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (productos.get(index) != productos.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}