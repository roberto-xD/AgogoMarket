package com.passioagogo.market.presentation.view.components

import android.util.Log
import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.model.venta.ProductoVenta

@Composable
fun ProductosSection(
    productos: List<ProductoVenta>,
    searchInput:(input: String) -> Unit,
    onEliminarProducto: (Long) -> Unit,
    onCantidadChange: (Long, Int) -> Unit,
    errorStock: String?
) {
    val showInputSearch = remember { mutableStateOf(false) }
    val showScanner = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                IconButton(
//                    onClick = {
//                        showInputSearch.value = showInputSearch.value.not()
//                    },
//                    modifier = Modifier
//                        .padding(4.dp)
//                        .background(
//                            Color.Black.copy(alpha = 0.5f),
//                            RoundedCornerShape(50)
//                        )
//                        .size(32.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.database_search),
//                        contentDescription = "buscar_sku",
//                        tint = Color.Black,
//                        modifier = Modifier.size(16.dp),
//                    )
//                }
                IconButton(
                    onClick = {
                        showInputSearch.value = false
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(50)
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.barcode_scann),
                        contentDescription = "buscar_codigo",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(
                    onClick = {
                        showInputSearch.value = showInputSearch.value.not()
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(50)
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_24),
                        contentDescription = "buscar_nombre",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            if(showInputSearch.value){
                SearchInput {
                    searchInput(it)
                }
            }

            if (errorStock != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorStock,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (productos.isEmpty()) {
                Text(
                    text = "No hay productos agregados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                productos.forEach { producto ->
                    ProductoVentaItem(
                        producto = producto,
                        onEliminar = { onEliminarProducto(producto.productoId) },
                        onCantidadChange = { cantidad ->
                            onCantidadChange(producto.productoId, cantidad)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}