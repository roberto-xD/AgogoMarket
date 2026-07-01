package com.passioagogo.market.presentation.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

// BarraNavegacionInferior.kt
@Composable
fun BarraNavegacionInferior(
    destinoActual: String = "inventario",
    alNavegar: (String) -> Unit = {}
) {
    val elementos = listOf(
        ElementoNav("inicio",      "Inicio",     Icons.Default.Home),
        ElementoNav("inventario",  "Inventario", Icons.Default.Phone),
        ElementoNav("pedidos",     "Pedidos",    Icons.Default.DateRange),
        ElementoNav("proveedores", "Proveedores",Icons.Default.MailOutline),
        ElementoNav("mas",         "Más",        Icons.Default.Add)
    )

    NavigationBar(containerColor = Color.White) {
        elementos.forEach { elemento ->
            NavigationBarItem(
                selected = destinoActual == elemento.ruta,
                onClick = { alNavegar(elemento.ruta) },
                icon = { Icon(elemento.icono, contentDescription = elemento.etiqueta) },
                label = { Text(elemento.etiqueta, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2979FF),
                    selectedTextColor = Color(0xFF2979FF),
                    indicatorColor = Color(0xFFE6F1FB),
                    unselectedIconColor = Color(0xFF888888),
                    unselectedTextColor = Color(0xFF888888)
                )
            )
        }
    }
}

data class ElementoNav(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)