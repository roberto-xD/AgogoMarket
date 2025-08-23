package com.passioagogo.market.presentation.view.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passioagogo.market.presentation.viewModel.products.ConfiguracionImportacion

@Composable
fun ConfiguracionImportacionCard(
    configuracion: ConfiguracionImportacion,
    onConfiguracionChange: (ConfiguracionImportacion) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "3. Configuración de Importación",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Validar duplicados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Validar duplicados")
                    Text(
                        text = "Verificar SKUs duplicados antes de importar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = configuracion.validarDuplicados,
                    onCheckedChange = {
                        onConfiguracionChange(configuracion.copy(validarDuplicados = it))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Saltear duplicados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Saltear duplicados")
                    Text(
                        text = "Ignorar productos con SKU existente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = configuracion.saltearDuplicados,
                    onCheckedChange = {
                        onConfiguracionChange(configuracion.copy(saltearDuplicados = it))
                    },
                    enabled = configuracion.validarDuplicados
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actualizar existentes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Actualizar existentes")
                    Text(
                        text = "Sobrescribir productos existentes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = configuracion.actualizarExistentes,
                    onCheckedChange = {
                        onConfiguracionChange(configuracion.copy(actualizarExistentes = it))
                    },
                    enabled = configuracion.validarDuplicados && !configuracion.saltearDuplicados
                )
            }
        }
    }
}