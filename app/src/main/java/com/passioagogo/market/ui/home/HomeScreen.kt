package com.passioagogo.market.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.common.UserRole

/**
 * Placeholder de Home: muestra la sesión activa y el aviso de
 * vendedor sin tienda. Será reemplazado por la navegación real
 * (POS / administración) en la siguiente fase.
 */
@Composable
fun HomeScreen(
    session: SessionState.Authenticated,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Hola, ${session.profile.nombre}",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = when (session.profile.rol) {
                UserRole.ADMIN -> "Administrador"
                UserRole.VENDEDOR -> "Vendedor"
                UserRole.CLIENTE -> "Cliente"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (session.vendedorSinTienda) {
            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "No tienes tienda asignada. Pide a un administrador " +
                        "que te asigne una para poder operar el punto de venta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = onSignOut) { Text("Cerrar sesión") }
    }
}
