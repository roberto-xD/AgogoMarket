package com.passioagogo.market.ui.admin.envios

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Lock
import com.passioagogo.market.ui.admin.customers.DesbloquearClaveDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.data.envio.EnvioSolicitud
import com.passioagogo.market.data.envio.GuiaPendiente
import java.text.NumberFormat
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

private enum class PestanaEnvios(val etiqueta: String) {
    PENDIENTES("Por generar"),
    GUIAS("Guías"),
    RECOLECCION("Recolección"),
}

@Composable
fun EnviosScreen(
    onBack: () -> Unit,
    viewModel: EnviosViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val pestanas = PestanaEnvios.entries
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if (pestana != 0) pestana = 0 else onBack()
    }

    Column(Modifier.fillMaxSize()) {
        // Sin la clave no se puede leer ninguna dirección, así que no se
        // puede generar ninguna guía: conviene decirlo antes de intentarlo.
        if (!state.claveDesbloqueada) {
            // La tarjeta abre el diálogo: antes solo informaba y obligaba
            // a ir a Clientes y volver.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { viewModel.onAbrirClave() },
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Clave de administración cerrada",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            "Sin ella no se pueden leer las direcciones. " +
                                "Toca para abrirla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }

        TabRow(selectedTabIndex = pestana) {
            pestanas.forEachIndexed { indice, p ->
                val cuantos = when (p) {
                    PestanaEnvios.PENDIENTES -> state.pendientes.size
                    PestanaEnvios.GUIAS -> state.conGuia.size
                    PestanaEnvios.RECOLECCION -> state.porRecoger.size
                }
                Tab(
                    selected = pestana == indice,
                    onClick = { pestana = indice },
                    text = { Text(if (cuantos > 0) "${p.etiqueta} ($cuantos)" else p.etiqueta) },
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        }

        when (pestanas[pestana]) {
            PestanaEnvios.PENDIENTES -> PendientesTab(state, viewModel)
            PestanaEnvios.GUIAS -> GuiasTab(state, context, viewModel::onPedirCancelarGuia)
            PestanaEnvios.RECOLECCION -> RecoleccionTab(state, viewModel)
        }
    }

    // ---- Diálogos ----

    state.guiaGenerada?.let { guia ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarGuia,
            title = { Text(if (guia.yaTenia) "Ya tenía guía" else "Guía generada") },
            text = {
                Column {
                    if (guia.yaTenia) {
                        Text(
                            "Esta solicitud ya tenía guía: no se generó otra ni se " +
                                "cobró de nuevo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(guia.numero, style = MaterialTheme.typography.titleMedium)
                    Text(
                        listOfNotNull(
                            guia.paqueteria.ifBlank { null },
                            guia.servicio.ifBlank { null },
                            guia.costo.takeIf { it > 0 }?.let { moneda.format(it) },
                        ).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    state.aviso?.let {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = guia.url.isNotBlank(),
                    onClick = {
                        abrir(context, guia.url)
                        viewModel.onCerrarGuia()
                    },
                ) { Text("Abrir etiqueta") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCerrarGuia) { Text("Cerrar") }
            },
        )
    }

    state.recoleccionHecha?.let { rec ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarRecoleccion,
            title = { Text("Recolección agendada") },
            text = {
                Column {
                    Text("Confirmación: ${rec.confirmacion}")
                    Text(
                        "${rec.paqueteria} · ${rec.fecha} · ${rec.desde} a ${rec.hasta}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "${rec.guias} guía(s) en esta recolección.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    state.aviso?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarRecoleccion) { Text("Entendido") }
            },
        )
    }

    state.cancelando?.let { solicitud ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarCancelacion,
            title = { Text("Cancelar la guía") },
            text = {
                Column {
                    Text("Guía ${solicitud.guiaNumero} de ${solicitud.guiaPaqueteria}")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Cancelar y recuperar el dinero no son lo mismo: una guía ya " +
                            "escaneada no se cancela, y cada paquetería tiene su " +
                            "plazo. Te diremos si hubo devolución.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "La solicitud volverá a quedarse sin guía y podrás generar " +
                            "otra.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onCancelarGuia(solicitud) }) {
                    Text("Cancelar guía")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCerrarCancelacion) { Text("Volver") }
            },
        )
    }

    state.cancelacionHecha?.let { cancelacion ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarCancelacionHecha,
            title = { Text("Guía cancelada") },
            text = {
                Column {
                    Text("${cancelacion.numero} · ${cancelacion.paqueteria}")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (cancelacion.devuelto) {
                            "La paquetería confirmó la devolución" +
                                (cancelacion.costo?.let { " de ${moneda.format(it)}" }
                                    ?: "") +
                                cancelacion.fechaDevolucion.ifBlank { "" }
                                    .let { if (it.isBlank()) "." else " el ${it.take(10)}." }
                        } else {
                            "La guía quedó cancelada, pero la paquetería NO confirmó " +
                                "devolución del importe. Revísalo en su panel si " +
                                "esperabas recuperarlo."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (cancelacion.devuelto) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                    state.aviso?.let {
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
                TextButton(onClick = viewModel::onCerrarCancelacionHecha) {
                    Text("Entendido")
                }
            },
        )
    }

    if (state.pidiendoClave) {
        DesbloquearClaveDialog(
            tieneArchivo = state.tieneArchivoClave,
            isWorking = state.abriendoClave,
            errorMessage = state.errorClave,
            onDismiss = viewModel::onCerrarClave,
            onDesbloquear = viewModel::onDesbloquear,
            onImportar = viewModel::onImportarClave,
        )
    }

    state.cambiandoPaqueteria?.let { solicitud ->
        var carrier by remember(solicitud.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::onCancelarCambio,
            title = { Text("Enviar por otra paquetería") },
            text = {
                Column {
                    Text(
                        "El cliente eligió " +
                            (solicitud.envioPaqueteria?.ifBlank { null }
                                ?: "una paquetería") +
                            " al cotizar.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Quien elige paquetería muchas veces no está eligiendo " +
                            "precio sino en quién confía. Cámbiala solo si hace " +
                            "falta —no cubre la ruta, rechaza la guía— y avísale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = carrier,
                        onValueChange = { carrier = it },
                        label = { Text("Paquetería") },
                        placeholder = { Text("estafeta, fedex, paquetexpress…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = carrier.isNotBlank(),
                    onClick = {
                        viewModel.onGenerarConOtraPaqueteria(solicitud, carrier)
                    },
                ) { Text("Generar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelarCambio) { Text("Cancelar") }
            },
        )
    }

    state.errorMessage?.let { mensaje ->
        val contexto = LocalContext.current
        AlertDialog(
            onDismissRequest = viewModel::onCerrarError,
            title = { Text("No se pudo completar") },
            text = {
                // Desplazable: la paquetería devuelve mensajes largos y
                // recortarlos esconde justo el dato que hace falta.
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(mensaje)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "El detalle completo está en el registro, con la etiqueta PG.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarError) { Text("Entendido") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val cb = contexto.getSystemService(Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                    cb.setPrimaryClip(
                        android.content.ClipData.newPlainText("Error", mensaje)
                    )
                    Toast.makeText(contexto, "Error copiado", Toast.LENGTH_SHORT).show()
                }) { Text("Copiar") }
            },
        )
    }
}

// ============ Por generar ============

@Composable
private fun PendientesTab(state: EnviosUiState, viewModel: EnviosViewModel) {
    if (state.isLoading && state.pendientes.isEmpty()) {
        Centrado { CircularProgressIndicator() }
        return
    }
    if (state.pendientes.isEmpty()) {
        Centrado {
            Text(
                "No hay envíos esperando guía.\n\nAquí solo salen las solicitudes " +
                    "hechas por el cliente desde el sitio, que son las únicas con " +
                    "dirección cifrada.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyColumn {
        item {
            Text(
                "Solo aparecen las solicitudes que hizo el propio cliente desde el " +
                    "sitio: son las que traen dirección cifrada. Las de promotor " +
                    "aún no se despachan desde aquí.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                "Marca «recoger» si quieres que la paquetería pase por el paquete. " +
                    "La recolección se pide después, en lote.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
        }
        items(state.pendientes, key = { it.id }) { solicitud ->
            val generando = state.generando == solicitud.id
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    "#${solicitud.folio} · ${solicitud.clienteNombre}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    listOfNotNull(
                        solicitud.envioPaqueteria?.ifBlank { null },
                        solicitud.envioServicio?.ifBlank { null },
                        solicitud.clienteTelefono?.ifBlank { null },
                    ).joinToString("  ·  ").ifBlank { "Sin paquetería elegida al cotizar" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = solicitud.id in state.marcadasParaRecoger,
                        onCheckedChange = {
                            viewModel.onMarcarRecoleccion(solicitud.id, it)
                        },
                    )
                    Text("Recoger", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    // Solo se deshabilita el botón de la fila en curso. Antes
                    // la condición miraba `state.generando == null`, así que
                    // al pulsar uno TODOS se atenuaban a la vez y parecía
                    // que se habían pulsado todos. El ViewModel ignora
                    // llamadas simultáneas, así que no hace falta apagarlos.
                    // Discreto a propósito: cambiar de paquetería es la
                    // excepción, no la vía normal.
                    TextButton(
                        onClick = { viewModel.onPedirCambioPaqueteria(solicitud) },
                        enabled = !generando && state.claveDesbloqueada,
                    ) { Text("Otra") }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = { viewModel.onGenerarGuia(solicitud) },
                        enabled = !generando && state.claveDesbloqueada,
                    ) {
                        Text(if (generando) "Generando…" else "Generar guía")
                    }
                }
            }
            HorizontalDivider()
        }
    }
}

// ============ Guías ============

@Composable
private fun GuiasTab(
    state: EnviosUiState,
    context: Context,
    onCancelar: (EnvioSolicitud) -> Unit,
) {
    if (state.conGuia.isEmpty()) {
        Centrado {
            Text(
                "Todavía no se ha generado ninguna guía.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn {
        items(state.conGuia, key = { it.id }) { solicitud ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "#${solicitud.folio} · ${solicitud.clienteNombre}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        solicitud.guiaNumero.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        listOfNotNull(
                            solicitud.guiaPaqueteria?.ifBlank { null },
                            solicitud.guiaServicio?.ifBlank { null },
                            solicitud.guiaCosto?.takeIf { it > 0 }?.let { moneda.format(it) },
                            solicitud.guiaGeneradaEn?.take(10),
                        ).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Estado de la recolección: distingue lo que espera
                    // camión de lo que se lleva a sucursal.
                    Text(
                        when {
                            !solicitud.recoleccionId.isNullOrBlank() ->
                                "Recolección pedida · ${solicitud.recoleccionId}"
                            solicitud.necesitaRecoleccion -> "Esperando recolección"
                            else -> "Se entrega en sucursal"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                solicitud.guiaUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    IconButton(onClick = { abrir(context, url) }) {
                        Icon(Icons.Filled.Print, contentDescription = "Abrir etiqueta")
                    }
                }
                IconButton(onClick = { onCancelar(solicitud) }) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = "Cancelar guía",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

// ============ Recolección ============

@Composable
private fun RecoleccionTab(state: EnviosUiState, viewModel: EnviosViewModel) {
    var confirmando by remember { mutableStateOf<String?>(null) }

    if (state.porRecoger.isEmpty()) {
        Centrado {
            Text(
                "Nada esperando recolección.\nLo que se lleva a sucursal no aparece aquí.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyColumn {
        item {
            Text(
                "Una recolección por paquetería: la paquetería agrupa las guías que " +
                    "salen del mismo sitio, así que pedir una por pedido sería un " +
                    "camión por paquete.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        state.porPaqueteria.forEach { (paqueteria, guias) ->
            item(key = "h-$paqueteria") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            paqueteria.ifBlank { "Sin paquetería" },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "${guias.size} guía(s) esperando",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = { confirmando = paqueteria },
                        enabled = !state.pidiendoRecoleccion,
                    ) { Text("Pedir recolección") }
                }
            }
            items(guias, key = { it.id }) { guia ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 16.dp, bottom = 8.dp),
                ) {
                    Column {
                        Text(
                            "#${guia.folio} · ${guia.cliente}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            guia.numero,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item(key = "d-$paqueteria") { HorizontalDivider() }
        }
    }

    confirmando?.let { paqueteria ->
        val fecha = remember { fechaManana() }
        AlertDialog(
            onDismissRequest = { confirmando = null },
            title = { Text("Pedir recolección") },
            text = {
                Column {
                    Text(
                        "Se pedirá a $paqueteria que pase mañana ($fecha) por todas " +
                            "las guías que están esperando.",
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No todas las paqueterías recogen: si esta no lo hace, lo " +
                            "dirá y habrá que llevar los paquetes a sucursal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onSolicitarRecoleccion(paqueteria, fecha)
                    confirmando = null
                }) { Text("Pedir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = null }) { Text("Cancelar") }
            },
        )
    }
}

/** Abre la etiqueta en el visor de PDF del teléfono. */
private fun abrir(context: Context, url: String) {
    if (url.isBlank()) return
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No hay una app para abrir la etiqueta",
            Toast.LENGTH_SHORT,
        ).show()
    }
}

@Composable
private fun Centrado(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
