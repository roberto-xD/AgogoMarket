package com.passioagogo.market.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passioagogo.market.domain.model.venta.ProductoVenta

@Composable
fun ProductoVentaItem(
    producto: ProductoVenta,
    onEliminar: () -> Unit,
    onCantidadChange: (Int) -> Unit
) {
    val stockInsuficiente = producto.cantidad > producto.stockDisponible

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (stockInsuficiente) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Precio: $${String.format("%.2f", producto.precioUnitario)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Stock: ${producto.stockDisponible}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stockInsuficiente) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (stockInsuficiente) {
                    Text(
                        text = "⚠️ Stock insuficiente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Controles de cantidad
                IconButton(
                    onClick = { onCantidadChange(producto.cantidad - 1) },
                    enabled = producto.cantidad > 1
                ) {
                    Icon(Icons.Default.Delete, "Disminuir")
                }

                Text(
                    text = producto.cantidad.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = { onCantidadChange(producto.cantidad + 1) }
                ) {
                    Icon(Icons.Default.Add, "Aumentar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Subtotal
                Text(
                    text = "$${String.format("%.2f", producto.subtotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(80.dp)
                )

                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}