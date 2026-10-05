package com.passioagogo.market.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
internal fun DrawerHeader(
    nombre: String,
    rol: String
) {
    Column(Modifier.padding(24.dp)) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = rol,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}