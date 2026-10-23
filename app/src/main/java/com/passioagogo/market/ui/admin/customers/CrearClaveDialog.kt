package com.passioagogo.market.ui.admin.customers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Crea un par de claves nuevo.
 *
 * Dos avisos que no son adorno:
 *
 * 1. Si ya hay direcciones guardadas con otra clave, **esta no las abre**.
 *    Generar un par no re-envuelve lo existente.
 * 2. Si se pierde la contraseña maestra, no hay recuperación posible.
 *    Nadie, ni el proveedor de la base, puede abrir esas direcciones.
 */
@Composable
fun CrearClaveDialog(
    direccionesExistentes: Int,
    isWorking: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCrear: (contrasena: String, id: String) -> Unit,
) {
    var contrasena by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var id by remember { mutableStateOf(sugerirId()) }
    var verClave by remember { mutableStateOf(false) }
    var entiendoRiesgo by remember { mutableStateOf(false) }

    val idValido = id.matches(Regex("^[a-z0-9][a-z0-9-]{1,38}$"))
    val claveValida = contrasena.length >= 12
    val coinciden = contrasena.isNotEmpty() && contrasena == repetir
    val listo = idValido && claveValida && coinciden && entiendoRiesgo

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear clave de administración") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Se genera un par de claves. La pública se registra en la " +
                        "base para que el sitio cifre con ella; la privada queda " +
                        "en este dispositivo, protegida con tu contraseña maestra.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Generar una clave nueva con direcciones ya guardadas las
                // deja inaccesibles salvo que se conserve la privada vieja.
                if (direccionesExistentes > 0) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            "Ya hay $direccionesExistentes dirección(es) guardadas " +
                                "con otra clave. Esta clave nueva NO podrá abrirlas: " +
                                "seguirás necesitando el archivo anterior para " +
                                "consultarlas. Si lo que quieres es recuperar el " +
                                "acceso a las existentes, cancela e importa tu " +
                                "archivo en lugar de crear uno.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it.lowercase() },
                    label = { Text("Identificador de la clave") },
                    singleLine = true,
                    isError = id.isNotBlank() && !idValido,
                    supportingText = {
                        Text(
                            if (id.isNotBlank() && !idValido) {
                                "Solo minúsculas, dígitos y guiones"
                            } else {
                                "Sirve para distinguirla si algún día la rotas"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña maestra") },
                    singleLine = true,
                    isError = contrasena.isNotEmpty() && !claveValida,
                    visualTransformation = if (verClave) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { verClave = !verClave }) {
                            Text(if (verClave) "Ocultar" else "Ver")
                        }
                    },
                    supportingText = { Text("Al menos 12 caracteres") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = repetir,
                    onValueChange = { repetir = it },
                    label = { Text("Repite la contraseña") },
                    singleLine = true,
                    isError = repetir.isNotEmpty() && !coinciden,
                    visualTransformation = if (verClave) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    supportingText = {
                        Text(if (repetir.isNotEmpty() && !coinciden) "No coinciden" else "")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(
                        "Sin esta contraseña no hay forma de abrir las direcciones. " +
                            "No se puede restablecer ni recuperar: guárdala en tu " +
                            "gestor de contraseñas antes de continuar.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = entiendoRiesgo,
                        onCheckedChange = { entiendoRiesgo = it },
                    )
                    Text(
                        "Entiendo que si pierdo la contraseña pierdo las direcciones",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = listo && !isWorking,
                onClick = { onCrear(contrasena, id.trim()) },
            ) { Text(if (isWorking) "Generando…" else "Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/**
 * Qué hacer tras generar: registrar la pública y respaldar el archivo.
 *
 * Se insiste en exportar porque el archivo vive en el almacenamiento
 * privado de la app: desinstalarla sin copia deja las direcciones
 * ilegibles.
 */
@Composable
fun ClaveCreadaDialog(
    publicaB64: String,
    registrada: Boolean,
    onExportar: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { /* obliga a usar el botón: hay un paso pendiente */ },
        title = { Text("Clave creada") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    if (registrada) {
                        "La clave pública quedó registrada en la base. El sitio ya " +
                            "cifrará las direcciones nuevas con ella."
                    } else {
                        "No se pudo registrar la pública en la base. Cópiala y " +
                            "pégala a mano en la tabla crypto_keys, o nadie podrá " +
                            "cifrar direcciones nuevas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (registrada) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error,
                )

                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Exporta el archivo ahora",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            "La clave privada vive solo en este dispositivo. Si " +
                                "desinstalas la app o cambias de teléfono sin una " +
                                "copia, las direcciones guardadas no se recuperan. " +
                                "Guárdala en tu gestor de contraseñas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = {
                    copiarTexto(context, "Clave pública", publicaB64)
                }) { Text("Copiar clave pública") }
            }
        },
        confirmButton = {
            TextButton(onClick = onExportar) { Text("Exportar archivo") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Más tarde") }
        },
    )
}

private fun copiarTexto(context: Context, etiqueta: String, texto: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(etiqueta, texto))
    Toast.makeText(context, "$etiqueta copiada", Toast.LENGTH_SHORT).show()
}

/** Identificador por omisión: fecha de creación, fácil de ordenar. */
private fun sugerirId(): String {
    val f = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
    return "admin-${f.format(java.util.Date())}"
}
