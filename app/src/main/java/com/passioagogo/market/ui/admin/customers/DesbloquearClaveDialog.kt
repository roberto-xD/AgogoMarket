package com.passioagogo.market.ui.admin.customers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Pide la contraseña maestra para abrir las direcciones.
 *
 * Si el dispositivo aún no tiene el archivo `admin-privada.json`, ofrece
 * importarlo. El archivo se guarda cifrado en el almacenamiento privado
 * de la app; el secreto real es la contraseña, que nunca se persiste.
 */
@Composable
fun DesbloquearClaveDialog(
    tieneArchivo: Boolean,
    isWorking: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onDesbloquear: (contrasena: String) -> Unit,
    onImportar: (contenidoJson: String, contrasena: String) -> Unit,
) {
    val context = LocalContext.current
    var contrasena by remember { mutableStateOf("") }
    var verClave by remember { mutableStateOf(false) }
    var archivoJson by remember { mutableStateOf<String?>(null) }
    var errorArchivo by remember { mutableStateOf<String?>(null) }

    val selector = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        errorArchivo = null
        archivoJson = runCatching {
            context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() }
        }.getOrNull() ?: run {
            errorArchivo = "No se pudo leer el archivo"
            null
        }
    }

    val listo = contrasena.length >= 12 && (tieneArchivo || archivoJson != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tieneArchivo) "Abrir direcciones" else "Importar clave") },
        text = {
            Column {
                Text(
                    if (tieneArchivo) {
                        "Introduce tu contraseña maestra para descifrar las " +
                            "direcciones. Se mantiene abierta mientras dure la sesión."
                    } else {
                        "Selecciona el archivo admin-privada.json y escribe la " +
                            "contraseña maestra con la que lo generaste. El archivo " +
                            "se guarda cifrado en este dispositivo; la contraseña no."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!tieneArchivo) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        // Sin filtro MIME estricto: algunos gestores marcan
                        // el .json como octet-stream y lo ocultarían.
                        onClick = { selector.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (archivoJson == null) "Elegir archivo"
                            else "Archivo seleccionado ✓"
                        )
                    }
                    errorArchivo?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña maestra") },
                    singleLine = true,
                    visualTransformation = if (verClave) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { verClave = !verClave }) {
                            Text(if (verClave) "Ocultar" else "Ver")
                        }
                    },
                    supportingText = {
                        Text(
                            if (contrasena.isNotEmpty() && contrasena.length < 12) {
                                "Al menos 12 caracteres"
                            } else ""
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

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
                onClick = {
                    val json = archivoJson
                    if (json != null) onImportar(json, contrasena)
                    else onDesbloquear(contrasena)
                },
            ) { Text(if (isWorking) "Abriendo…" else "Abrir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
