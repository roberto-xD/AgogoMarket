package com.passioagogo.market.ui.admin.codigos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import com.passioagogo.market.ui.common.AccionDeslizable
import com.passioagogo.market.ui.common.TarjetaDeslizable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.data.codigos.Codigo
import com.passioagogo.market.data.codigos.TipoCodigo
import com.passioagogo.market.ui.common.OpcionesTeclado

/**
 * Los códigos que se canjean al pagar.
 *
 * La ruleta emite el suyo al girar; aquí se reparten los de campaña, que
 * hasta ahora se insertaban por SQL.
 */
@Composable
fun CodigosScreen(
    onBack: () -> Unit,
    viewModel: CodigosViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = true) { onBack() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.soloManuales,
                    onClick = { viewModel.onSoloManuales(!state.soloManuales) },
                    label = { Text("Solo de campaña") },
                )
                FilterChip(
                    selected = state.soloCanjeables,
                    onClick = { viewModel.onSoloCanjeables(!state.soloCanjeables) },
                    label = { Text("Solo canjeables") },
                )
            }

            Text(
                "${state.canjeables} canjeable(s) de ${state.codigos.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading && state.codigos.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }

                state.visibles.isEmpty() -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    Alignment.Center,
                ) {
                    Text(
                        "No hay códigos que mostrar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn {
                    items(state.visibles, key = { it.id }) { codigo ->
                        TarjetaDeslizable(
                            acciones = listOf(
                                AccionDeslizable(
                                    etiqueta = "Editar",
                                    icono = Icons.Filled.Edit,
                                    color = Color(0xFF1976D2),
                                    onClick = { viewModel.onEditar(codigo) },
                                ),
                                AccionDeslizable(
                                    etiqueta = "Ocultar",
                                    icono = Icons.Filled.VisibilityOff,
                                    color = Color(0xFFE53935),
                                    onClick = { viewModel.onPedirOcultar(codigo) },
                                ),
                            ),
                            // Abrir una recoge las demás
                            cerrar = state.deslizada,
                            onClick = {
                                viewModel.onDeslizada(codigo.id)
                                copiar(context, codigo.codigo)
                            },
                        ) {
                            FilaCodigo(
                                codigo = codigo,
                                ahoraIso = state.ahoraIso,
                                onCambiarActivo = { viewModel.onCambiarActivo(codigo) },
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::onAbrirAlta,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nuevo código")
        }
    }

    state.editando?.let { codigo ->
        EditarCodigoDialog(
            codigo = codigo,
            edicion = state.edicion,
            isSaving = state.isSaving,
            onCambiar = viewModel::onEdicion,
            onGuardar = viewModel::onGuardarEdicion,
            onDismiss = viewModel::onCerrarEdicion,
        )
    }

    state.porOcultar?.let { codigo ->
        AlertDialog(
            onDismissRequest = viewModel::onCancelarOcultar,
            title = { Text("Ocultar ${codigo.codigo}") },
            text = {
                Column {
                    Text("Desaparece de esta lista, pero no se borra de la base.")
                    // La distinción que evita el error caro: esconder no
                    // es cancelar.
                    if (codigo.canjeable(state.ahoraIso)) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Text(
                                "Ojo: este código sigue activo y se seguirá " +
                                    "canjeando. Para que deje de valer, apágalo " +
                                    "con el interruptor.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onOcultar) { Text("Ocultar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelarOcultar) { Text("Cancelar") }
            },
        )
    }

    if (state.creando) {
        AltaCodigoDialog(
            formulario = state.formulario,
            isSaving = state.isSaving,
            onCambiar = viewModel::onFormulario,
            onCrear = viewModel::onCrear,
            onDismiss = viewModel::onCerrarAlta,
        )
    }

    state.errorMessage?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarError,
            title = { Text("No se pudo completar") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarError) { Text("Entendido") }
            },
        )
    }
}

@Composable
private fun FilaCodigo(
    codigo: Codigo,
    ahoraIso: String,
    onCambiarActivo: () -> Unit,
) {
    val canjeable = codigo.canjeable(ahoraIso)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                codigo.codigo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (canjeable) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                codigo.detalle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                listOfNotNull(
                    "${codigo.usos}/${codigo.usosMax} usos",
                    codigo.expiraAt?.take(10)?.let { "caduca $it" },
                    codigo.origen.takeIf { it == "ruleta" },
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Por qué no se puede canjear: las tres razones son distintas
            // y llevan a decisiones distintas.
            if (!canjeable) {
                Text(
                    when {
                        !codigo.activo -> "Apagado"
                        codigo.agotado -> "Agotado"
                        else -> "Caducado"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            codigo.nota?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Apagar no borra: un código repartido que hay que cortar sigue
        // existiendo para saber qué se prometió.
        Switch(checked = codigo.activo, onCheckedChange = { onCambiarActivo() })
    }
}

@Composable
private fun AltaCodigoDialog(
    formulario: FormularioCodigo,
    isSaving: Boolean,
    onCambiar: (FormularioCodigo) -> Unit,
    onCrear: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo código") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = formulario.codigo,
                    onValueChange = { onCambiar(formulario.copy(codigo = it.uppercase())) },
                    label = { Text("Código") },
                    placeholder = { Text("NOV15") },
                    singleLine = true,
                    isError = formulario.codigo.isNotBlank() && !formulario.codigoValido,
                    supportingText = {
                        Text(
                            if (formulario.codigo.isNotBlank() && !formulario.codigoValido) {
                                "Entre 4 y 24 caracteres"
                            } else {
                                "Se dicta por teléfono: evita caracteres confusos"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))
                Text("Qué da", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoCodigo.entries.forEach { t ->
                        FilterChip(
                            selected = formulario.tipo == t,
                            onClick = { onCambiar(formulario.copy(tipo = t)) },
                            label = { Text(t.etiqueta) },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                // Solo el campo del tipo elegido: la tabla rechaza una fila
                // con un porcentaje y un regalo a la vez.
                when (formulario.tipo) {
                    TipoCodigo.DESCUENTO -> OutlinedTextField(
                        value = formulario.descuentoPct,
                        onValueChange = { onCambiar(formulario.copy(descuentoPct = it)) },
                        label = { Text("Porcentaje") },
                        suffix = { Text("%") },
                        singleLine = true,
                        isError = !formulario.datosDelTipoValidos,
                        supportingText = { Text("De 1 a 100") },
                        keyboardOptions = OpcionesTeclado.Numero,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    TipoCodigo.REGALO -> OutlinedTextField(
                        value = formulario.regalo,
                        onValueChange = { onCambiar(formulario.copy(regalo = it)) },
                        label = { Text("Qué se mete en la caja") },
                        singleLine = true,
                        isError = !formulario.datosDelTipoValidos,
                        supportingText = {
                            Text("Lo leerá quien prepare el pedido")
                        },
                        keyboardOptions = OpcionesTeclado.Texto,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    TipoCodigo.ENVIO_GRATIS -> OutlinedTextField(
                        value = formulario.minimoCompra,
                        onValueChange = { onCambiar(formulario.copy(minimoCompra = it)) },
                        label = { Text("Compra mínima (opcional)") },
                        prefix = { Text("$") },
                        singleLine = true,
                        isError = !formulario.datosDelTipoValidos,
                        supportingText = {
                            Text("Sobre los artículos, nunca sobre el envío")
                        },
                        keyboardOptions = OpcionesTeclado.Decimal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formulario.usosMax,
                        onValueChange = { onCambiar(formulario.copy(usosMax = it)) },
                        label = { Text("Usos") },
                        singleLine = true,
                        isError = !formulario.usosValidos,
                        supportingText = { Text("1 = personal") },
                        keyboardOptions = OpcionesTeclado.Numero,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = formulario.diasVigencia,
                        onValueChange = { onCambiar(formulario.copy(diasVigencia = it)) },
                        label = { Text("Días") },
                        singleLine = true,
                        supportingText = { Text("Vacío = sin caducar") },
                        keyboardOptions = OpcionesTeclado.Numero,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = formulario.nota,
                    onValueChange = { onCambiar(formulario.copy(nota = it)) },
                    label = { Text("Nota") },
                    placeholder = { Text("Campaña de noviembre en Instagram") },
                    singleLine = true,
                    supportingText = {
                        Text("Para acordarte dentro de seis meses de qué era")
                    },
                    keyboardOptions = OpcionesTeclado.Texto,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulario.valido && !isSaving,
                onClick = onCrear,
            ) { Text(if (isSaving) "Creando…" else "Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

private fun copiar(context: Context, codigo: String) {
    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cb.setPrimaryClip(ClipData.newPlainText("Código", codigo))
    Toast.makeText(context, "$codigo copiado", Toast.LENGTH_SHORT).show()
}


/**
 * Edita lo que se puede cambiar sin romper lo ya prometido.
 *
 * El tipo y el porcentaje no aparecen: quien recibió «15%» tiene que
 * seguir recibiendo 15%.
 */
@Composable
private fun EditarCodigoDialog(
    codigo: Codigo,
    edicion: EdicionCodigo,
    isSaving: Boolean,
    onCambiar: (EdicionCodigo) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(codigo.codigo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    codigo.detalle,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Lo que da no se cambia: quien ya lo tiene cuenta con ello.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = edicion.diasVigencia,
                    onValueChange = { onCambiar(edicion.copy(diasVigencia = it)) },
                    label = { Text("Nueva vigencia") },
                    suffix = { Text("días") },
                    singleLine = true,
                    supportingText = {
                        Text(
                            codigo.expiraAt?.take(10)?.let { "Caduca el $it · vacío lo deja igual" }
                                ?: "Hoy no caduca · vacío lo deja igual"
                        )
                    },
                    keyboardOptions = OpcionesTeclado.Numero,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = edicion.usosMax,
                    onValueChange = { onCambiar(edicion.copy(usosMax = it)) },
                    label = { Text("Tope de usos") },
                    singleLine = true,
                    isError = !edicion.usosValidos,
                    supportingText = {
                        Text(
                            if (!edicion.usosValidos) "Al menos 1"
                            // Bajarlo por debajo de lo gastado lo agota al
                            // instante: conviene verlo antes de guardar.
                            else "Lleva ${codigo.usos} canjeado(s)"
                        )
                    },
                    keyboardOptions = OpcionesTeclado.Numero,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = edicion.nota,
                    onValueChange = { onCambiar(edicion.copy(nota = it)) },
                    label = { Text("Descripción") },
                    keyboardOptions = OpcionesTeclado.Texto,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = edicion.valido && !isSaving, onClick = onGuardar) {
                Text(if (isSaving) "Guardando…" else "Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
