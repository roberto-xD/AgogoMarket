package com.passioagogo.market.ui.admin.pagos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import com.passioagogo.market.ui.common.OpcionesTeclado
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.data.pagos.Cobro
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

/**
 * Lo cobrado por el sitio y lo que de verdad llegó.
 *
 * No es un estado de resultados: aquí no entra el costo de los
 * productos. Sirve para cuadrar lo que la pasarela ingresó contra lo
 * que se vendió, y para ver qué falta por liberar.
 */
@Composable
fun CobrosScreen(
    onBack: () -> Unit,
    viewModel: CobrosViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val ahora = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
    }

    var pestana by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = true) {
        if (pestana != 0) pestana = 0 else onBack()
    }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestana) {
            Tab(
                selected = pestana == 0,
                onClick = { pestana = 0 },
                text = { Text("Cobros") },
            )
            Tab(
                selected = pestana == 1,
                onClick = { pestana = 1 },
                text = {
                    val n = state.porReembolsar.size
                    Text(if (n > 0) "Por devolver ($n)" else "Por devolver")
                },
            )
        }

        if (pestana == 1) {
            TabDevoluciones(state, viewModel)
            return@Column
        }

        // ---- Filtros ----
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Periodo.entries.forEach { p ->
                FilterChip(
                    selected = state.periodo == p,
                    onClick = { viewModel.onPeriodo(p) },
                    label = { Text(p.etiqueta) },
                )
            }
            FilterChip(
                selected = state.soloAprobadas,
                onClick = { viewModel.onSoloAprobadas(!state.soloAprobadas) },
                label = { Text("Solo aprobadas") },
            )
        }

        // ---- Resumen ----
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "${state.ventas} venta(s) · ${state.periodo.etiqueta.lowercase()}",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(8.dp))
                Cifra("Cobrado", state.cobrado)
                Cifra("Comisiones", -state.comisiones)
                Cifra("Guías", -state.guias)
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Cifra("Neto tras guía", state.netoTrasGuia, destacado = true)
                Text(
                    "No es la ganancia: falta el costo de los productos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val porLiberar = state.porLiberar(ahora)
                if (porLiberar > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${moneda.format(porLiberar)} aprobados que la pasarela " +
                            "todavía no libera.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                // Si el neto falta, las cifras de arriba se quedan cortas:
                // conviene decirlo antes de que alguien las dé por buenas.
                if (state.sinNetoTodavia > 0) {
                    Text(
                        "${state.sinNetoTodavia} venta(s) sin neto informado aún: " +
                            "no entran en el neto tras guía.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            state.isLoading && state.cobros.isEmpty() ->
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

            state.cobros.isEmpty() -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                Alignment.Center,
            ) {
                Text(
                    "No hay cobros en este periodo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            else -> LazyColumn {
                items(state.cobros, key = { it.id }) { cobro ->
                    FilaCobro(cobro) { viewModel.onVerDetalle(cobro) }
                    HorizontalDivider()
                }
            }
        }
    }

    state.detalle?.let { c ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarDetalle,
            title = { Text("Venta #${c.folio}") },
            text = {
                Column {
                    Text(c.clienteNombre, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(12.dp))

                    Linea("Artículos y envío", c.totalEstimado)
                    c.envioCosto?.takeIf { it > 0 }?.let { Linea("Envío cobrado", it) }
                    if (c.envioGratis) {
                        Text(
                            "Envío promocional: lo absorbió la tienda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    c.codigoDescuento?.let {
                        Text(
                            "Código $it" + (c.descuentoPct?.let { p -> " · $p%" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    c.regalo?.let {
                        Text(
                            "Regalo: $it",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    c.pagoTotal?.let { Linea("Cobrado", it) }
                    Text(
                        listOfNotNull(
                            c.pagoMetodo,
                            c.pagoMarca,
                            c.pagoUltimos4?.let { "····$it" },
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    c.pagoComision?.let { Linea("Comisión", -it) }
                    c.guiaCosto?.let { Linea("Guía", -it) }
                    val neto = c.netoTrasGuia
                    if (neto != null) {
                        Linea("Neto tras guía", neto, destacado = true)
                    } else {
                        Text(
                            "La pasarela todavía no informa cuánto llegó.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    c.pagoLiberacion?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Se libera el ${it.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    c.pagoReferencia?.let {
                        Text(
                            "Referencia: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarDetalle) { Text("Cerrar") }
            },
        )
    }

    state.errorMessage?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarError,
            title = { Text("No se pudo cargar") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarError) { Text("Entendido") }
            },
        )
    }
}

@Composable
private fun FilaCobro(cobro: Cobro, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "#${cobro.folio} · ${cobro.clienteNombre}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                listOfNotNull(
                    cobro.pagoEstado,
                    cobro.pagoMetodo,
                    cobro.pagoActualizadoEn?.take(10),
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = if (cobro.aprobado) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                moneda.format(cobro.pagoTotal ?: 0.0),
                fontWeight = FontWeight.SemiBold,
            )
            cobro.netoTrasGuia?.let {
                Text(
                    "neto ${moneda.format(it)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Cifra(etiqueta: String, monto: Double, destacado: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            etiqueta,
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            moneda.format(monto),
            style = if (destacado) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (monto < 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun Linea(etiqueta: String, monto: Double, destacado: Boolean = false) =
    Cifra(etiqueta, monto, destacado)


/**
 * Cancelaciones que el cliente pidió y siguen cobradas.
 *
 * Devolver el dinero es una decisión de una persona, no un automatismo:
 * de ahí el botón y la confirmación con el importe a la vista.
 */
@Composable
private fun TabDevoluciones(state: CobrosUiState, viewModel: CobrosViewModel) {
    if (state.porReembolsar.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            Alignment.Center,
        ) {
            Text(
                "No hay cancelaciones esperando devolución.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyColumn {
        items(state.porReembolsar, key = { it.id }) { item ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "#${item.folio} · ${item.clienteNombre}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    listOfNotNull(
                        item.pagoTotal?.let { moneda.format(it) },
                        item.clienteTelefono,
                        item.solicitadaAt?.take(10),
                    ).joinToString("  ·  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                item.motivo?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "Motivo: $it",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // Con guía no se devuelve: el paquete puede ir en camino.
                // Primero se cancela la guía desde Envíos.
                if (item.bloqueadaPorGuia) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            "Tiene guía ${item.guiaNumero}. Cancélala primero en " +
                                "Envíos y guías: mientras exista, el paquete puede " +
                                "estar en camino.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.onPedirReembolso(item) },
                            enabled = !state.procesandoReembolso,
                        ) {
                            Text(
                                if (state.procesandoReembolso) "Devolviendo…"
                                else "Devolver el dinero",
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
        }
    }

    // ---- Confirmación ----
    state.reembolsando?.let { item ->
        var parcial by remember(item.id) { mutableStateOf("") }
        var devolverCodigo by remember(item.id) { mutableStateOf(true) }
        val montoParcial = parcial.toDoubleOrNull()
        val cobrado = item.pagoTotal ?: 0.0
        val parcialValida = parcial.isBlank() ||
            (montoParcial != null && montoParcial > 0 && montoParcial <= cobrado)

        AlertDialog(
            onDismissRequest = viewModel::onCancelarReembolso,
            title = { Text("Devolver #${item.folio}") },
            text = {
                Column {
                    Text(
                        "Se devolverán ${moneda.format(cobrado)} a " +
                            "${item.clienteNombre}.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = parcial,
                        onValueChange = { parcial = it },
                        label = { Text("Importe parcial (opcional)") },
                        placeholder = { Text("Vacío = todo") },
                        singleLine = true,
                        isError = !parcialValida,
                        supportingText = {
                            Text(
                                if (!parcialValida) {
                                    "Entre 0 y ${moneda.format(cobrado)}"
                                } else {
                                    "Una devolución parcial NO cancela la solicitud."
                                }
                            )
                        },
                        keyboardOptions = OpcionesTeclado.Decimal,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (!item.codigoDescuento.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = devolverCodigo,
                                onCheckedChange = { devolverCodigo = it },
                            )
                            Text(
                                "Devolver el código ${item.codigoDescuento}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            "Desactívalo solo si devuelves por abuso: en lo demás, " +
                                "a quien se le devuelve el dinero se le devuelve " +
                                "el premio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = parcialValida,
                    onClick = {
                        viewModel.onReembolsar(
                            item = item,
                            monto = montoParcial,
                            devolverCodigo = devolverCodigo,
                        )
                    },
                ) { Text("Devolver") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelarReembolso) { Text("Cancelar") }
            },
        )
    }

    // ---- Resultado ----
    state.reembolsoHecho?.let { r ->
        AlertDialog(
            onDismissRequest = viewModel::onCerrarReembolsoHecho,
            title = { Text(if (r.total) "Devolución hecha" else "Devolución parcial") },
            text = {
                Column {
                    Text("${moneda.format(r.monto)} · #${r.folio}")
                    if (!r.total) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "La solicitud NO queda cancelada: se devolvió una parte " +
                                "y la compra sigue en pie.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    r.codigoDevuelto?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "El código $it vuelve a estar disponible.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    // El dinero salió aunque no se anotara: hay que saberlo
                    if (!r.guardado) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "El dinero salió, pero no se pudo anotar en la " +
                                "solicitud. Apunta el reembolso ${r.reembolsoId}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCerrarReembolsoHecho) {
                    Text("Entendido")
                }
            },
        )
    }
}
