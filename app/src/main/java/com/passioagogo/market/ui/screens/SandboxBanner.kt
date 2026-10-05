package com.passioagogo.market.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Franja de advertencia del entorno de pruebas. */
@Composable
fun SandboxBanner() {
    Text(
        text = "ENTORNO DE PRUEBAS · los datos no son reales",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(vertical = 4.dp),
    )
}