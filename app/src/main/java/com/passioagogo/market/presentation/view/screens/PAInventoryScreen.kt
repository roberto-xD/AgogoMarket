package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.passioagogo.market.presentation.view.components.BarraNavegacionInferior
import com.passioagogo.market.presentation.view.components.SearchInput

// DatosEjemplo.kt
val productosEjemplo = listOf(
    Producto(
        id = "001",
        nombre = "Audífonos Inalámbricos X",
        categoria = "Electrónica",
        precioCompra = 75.00,
        precioVenta = 129.99,
        estado = ProductStatus.DISPONIBLE,
        descripcion = "Audífonos over-ear con cancelación de ruido activa y 30h de batería.",
        imagenUrl = "https://ejemplo.com/img/audifonos_x.png",
        proveedor = Proveedor(
            "TechSupply MX",
            "+52 55 1234 5678",
            "ventas@techsupply.mx",
            "https://wa.me/5215512345678"
        )
    ),
    Producto(
        id = "002",
        nombre = "Audífonos Inalámbricos Y",
        categoria = "Electrónica",
        precioCompra = 60.00,
        precioVenta = 99.99,
        estado = ProductStatus.STOCK_BAJO,
        descripcion = "Audífonos on-ear ligeros con micrófono integrado.",
        imagenUrl = "https://ejemplo.com/img/audifonos_y.png",
        proveedor = Proveedor(
            "AudioMex",
            "+52 33 9876 5432",
            "contacto@audiomex.mx",
            "https://wa.me/5213398765432"
        )
    ),
    Producto(
        id = "003",
        nombre = "Audífonos Inalámbricos Z",
        categoria = "Electrónica",
        precioCompra = 90.00,
        precioVenta = 149.99,
        estado = ProductStatus.SIN_STOCK,
        descripcion = "Audífonos premium con Hi-Res Audio y estuche de carga.",
        imagenUrl = "https://ejemplo.com/img/audifonos_z.png",
        proveedor = Proveedor(
            "TechSupply MX",
            "+52 55 1234 5678",
            "ventas@techsupply.mx",
            "https://wa.me/5215512345678"
        )
    ),
    Producto(
        id = "004",
        nombre = "Audífonos Sport W",
        categoria = "Electrónica",
        precioCompra = 40.00,
        precioVenta = 79.99,
        estado = ProductStatus.EN_PEDIDO,
        descripcion = "Audífonos resistentes al sudor para actividad física.",
        imagenUrl = "https://ejemplo.com/img/audifonos_w.png",
        proveedor = Proveedor(
            "SportGadgets",
            "+52 81 5555 0000",
            "info@sportgadgets.mx",
            "https://wa.me/5218155550000"
        )
    )
)

// InventoryScreen.kt
@Composable
fun InventoryScreen() {
    val chips = listOf("All", "Available", "Out of Stock", "On Order", "Supplier", "Limited", "Discontinued")
    var selectedChip by remember { mutableStateOf("All") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* navegar a AddProduct */ },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Product") },
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            )
        },
        bottomBar = { BarraNavegacionInferior() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
        ) {
            // Title
            Text(
                text = "Main Inventory",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Search bar
            SearchInput(
                doSearch = {},
                openScan = {}
            )

            Spacer(Modifier.height(12.dp))

            // Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items = chips ) { chip ->
                    StatusChip(
                        label = chip,
                        selected = chip == selectedChip,
                        onClick = { selectedChip = chip }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Product feed
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productosEjemplo ) { product ->
                    ProductCard(product = product)
                }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val (bg, textColor, border) = when {
        selected -> Triple(Color(0xFF2979FF), Color.White, Color.Transparent)
        label == "Disponible" -> Triple(Color(0xFFEAF3DE), Color(0xFF3B6D11), Color(0xFF97C459))
        label == "No disponible" -> Triple(Color(0xFFFCEBEB), Color(0xFFA32D2D), Color(0xFFF09595))
        label == "Bajo pedido" -> Triple(Color(0xFFE6F1FB), Color(0xFF185FA5), Color(0xFF85B7EB))
        label == "Pocas piezas" -> Triple(Color(0xFFFAEEDA), Color(0xFF854F0B), Color(0xFFEF9F27))
        else -> Triple(Color.White, Color(0xFF555555), Color(0xFFDDDDDD))
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(1.5.dp, border)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun ProductCard(product: Producto) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE8E8E8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(product.imgBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = product.imagenUrl, contentDescription = product.nombre)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(product.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(product.categoria, color = Color(0xFF888888), fontSize = 12.sp)
                Text("$${product.precioVenta}", color = Color(0xFF2979FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            StatusBadge(status = product.estado)
        }
    }
}

@Composable
fun StatusBadge(status: ProductStatus) {
    val (bg, textColor, dotColor, label) = when (status) {
        ProductStatus.DISPONIBLE -> listOf(Color(0xFFEAF3DE), Color(0xFF3B6D11), Color(0xFF3B6D11), ProductStatus.DISPONIBLE.etiqueta)
        ProductStatus.STOCK_BAJO -> listOf(Color(0xFFFAEEDA), Color(0xFF854F0B), Color(0xFFEF9F27),  ProductStatus.STOCK_BAJO.etiqueta)
        ProductStatus.SIN_STOCK -> listOf(Color(0xFFFCEBEB), Color(0xFFA32D2D), Color(0xFFE24B4A),  ProductStatus.SIN_STOCK.etiqueta)
        else -> listOf(Color(0xFFFCEBEB), Color(0xFFA32D2D), Color(0xFFE24B4A),  "")
    }
    Row(
        modifier = Modifier
            .background(bg as Color, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(Modifier.size(6.dp)) { drawCircle(dotColor as Color) }
        Text(label as String, color = textColor as Color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

enum class ProductStatus(val etiqueta: String) {
    DISPONIBLE("Disponible"),
    EN_PEDIDO("En pedido"),
    STOCK_BAJO("Stock bajo"),
    SIN_STOCK("Sin stock"),
    DESCONTINUADO("Descontinuado")
}

data class Producto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val precioCompra: Double,
    val precioVenta: Double,
    val estado: ProductStatus,
    val descripcion: String,
    val imagenUrl: String,
    val proveedor: Proveedor,
    val imgBackground: Color = Color(0xFFFFFFFF),
)

@Preview
@Composable
private fun Preview(){
    InventoryScreen()
}
