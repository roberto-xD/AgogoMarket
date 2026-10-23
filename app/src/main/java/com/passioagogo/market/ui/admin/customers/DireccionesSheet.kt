package com.passioagogo.market.ui.admin.customers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passioagogo.market.data.addresses.DireccionClara
import com.passioagogo.market.data.customers.Customer

/**
 * Muestra las direcciones ya descifradas de un cliente.
 *
 * Se abren en el momento de consultarlas y se descartan al cerrar: no se
 * guardan en caché ni se escriben en disco. El cifrado protege lo
 * almacenado, así que dejarlas residiendo en memoria lo contradiría.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DireccionesSheet(
    customer: Customer,
    direcciones: List<DireccionDescifrada>,
    cargando: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Direcciones", style = MaterialTheme.typography.titleLarge)
            Text(
                customer.nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            when {
                cargando -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                direcciones.isEmpty() -> Text(
                    "Este cliente aún no ha capturado ninguna dirección en el sitio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                )

                else -> direcciones.forEachIndexed { indice, dir ->
                    if (indice > 0) {
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    }
                    FichaDireccion(dir, context)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Estos datos están cifrados en la base de datos. Solo se " +
                    "descifran aquí, mientras esta ventana está abierta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FichaDireccion(dir: DireccionDescifrada, context: Context) {
    if (dir.noSePudoAbrir || dir.datos == null) {
        // Pasa tras rotar la clave: esta dirección se envolvió con la
        // anterior y hace falta aquella privada para abrirla.
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "  No se pudo abrir esta dirección con la clave actual. " +
                        "Si rotaste la clave de administración, hace falta la anterior.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
        return
    }

    val datos = dir.datos
    Column {
        dir.etiqueta?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            listOfNotNull(
                datos.calle.ifBlank { null },
                datos.interior.ifBlank { null }?.let { "int. $it" },
            ).joinToString(" "),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            listOf(datos.colonia, datos.cp).filter { it.isNotBlank() }
                .joinToString("  ·  CP "),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            listOf(datos.ciudad, datos.estado).filter { it.isNotBlank() }
                .joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (datos.referencias.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Referencias: ${datos.referencias}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { copiar(context, "Dirección", datos.enUnaLinea()) }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Text("  Copiar")
            }
            TextButton(onClick = { copiar(context, "Dirección", textoCompleto(datos)) }) {
                Text("Copiar con referencias")
            }
        }
    }
}

/** Formato de varias líneas, el que suele pedir la guía de paquetería. */
private fun textoCompleto(d: DireccionClara): String = buildString {
    appendLine(listOfNotNull(d.calle.ifBlank { null }, d.interior.ifBlank { null }
        ?.let { "int. $it" }).joinToString(" "))
    if (d.colonia.isNotBlank()) appendLine("Col. ${d.colonia}")
    if (d.cp.isNotBlank()) appendLine("CP ${d.cp}")
    appendLine(listOf(d.ciudad, d.estado).filter { it.isNotBlank() }.joinToString(", "))
    if (d.referencias.isNotBlank()) append("Ref: ${d.referencias}")
}.trim()

private fun copiar(context: Context, etiqueta: String, texto: String) {
    if (texto.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(etiqueta, texto))
    Toast.makeText(context, "$etiqueta copiada", Toast.LENGTH_SHORT).show()
}
