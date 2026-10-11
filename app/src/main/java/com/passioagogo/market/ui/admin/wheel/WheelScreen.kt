package com.passioagogo.market.ui.admin.wheel

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.data.wheel.WheelPrize
import com.passioagogo.market.data.wheel.WheelPrizeDraft
import com.passioagogo.market.data.wheel.WheelSpin
import com.passioagogo.market.data.wheel.WheelTerm

private enum class PestanaRuleta(val etiqueta: String) {
    PREMIOS("Premios"),
    CODIGOS("Códigos"),
    BASES("Bases"),
}

@Composable
fun WheelScreen(
    onBack: () -> Unit,
    viewModel: WheelViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val pestanas = PestanaRuleta.entries

    // Desde una pestaña distinta a la primera, «atrás» vuelve a Premios
    // antes de salir: evita abandonar la pantalla por accidente.
    BackHandler(enabled = true) {
        if (pestana != 0) pestana = 0 else onBack()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // ---------- Interruptor de la sección ----------
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Ruleta en la web", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (state.visible)
                                "Visible · ${state.pendientes} código(s) por canjear"
                            else "Oculta: nadie puede girar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.visible,
                        onCheckedChange = viewModel::onToggleVisible,
                    )
                }
            }

            TabRow(selectedTabIndex = pestana) {
                pestanas.forEachIndexed { indice, p ->
                    Tab(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        text = {
                            val sufijo = when (p) {
                                PestanaRuleta.CODIGOS ->
                                    if (state.pendientes > 0) " (${state.pendientes})" else ""
                                PestanaRuleta.BASES ->
                                    if (state.terminosPendientes > 0) " ⚠" else ""
                                else -> ""
                            }
                            Text(p.etiqueta + sufijo)
                        },
                    )
                }
            }

            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            when (pestanas[pestana]) {
                PestanaRuleta.PREMIOS -> PremiosTab(state, viewModel)
                PestanaRuleta.CODIGOS -> CodigosTab(state, viewModel)
                PestanaRuleta.BASES -> BasesTab(state, viewModel)
            }
        }

        if (pestanas[pestana] == PestanaRuleta.PREMIOS) {
            FloatingActionButton(
                onClick = viewModel::onNuevoPremio,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo premio")
            }
        }
    }

    if (state.creandoPremio || state.editandoPremio != null) {
        PremioDialog(
            existente = state.editandoPremio,
            isSaving = state.isSaving,
            onDismiss = viewModel::onCerrarPremio,
            onSave = { draft -> viewModel.onGuardarPremio(draft, state.editandoPremio) },
        )
    }

    state.editandoTermino?.let { term ->
        TerminoDialog(
            term = term,
            isSaving = state.isSaving,
            onDismiss = viewModel::onCerrarTermino,
            onSave = viewModel::onGuardarTermino,
        )
    }
}

// ============ Premios ============

@Composable
private fun PremiosTab(state: WheelUiState, viewModel: WheelViewModel) {
    if (state.isLoading && state.prizes.isEmpty()) {
        Centrado { CircularProgressIndicator() }
        return
    }

    LazyColumn {
        item {
            Text(
                "El peso es relativo: la probabilidad resulta de dividirlo entre " +
                    "la suma de los activos. Solo cuentan los premios activos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        items(state.prizes, key = { it.id }) { prize ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onEditarPremio(prize) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            prize.rotulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (!prize.otorgaPremio) {
                            Spacer(Modifier.width(8.dp))
                            AssistChip(
                                onClick = { viewModel.onEditarPremio(prize) },
                                label = { Text("Sin premio") },
                            )
                        }
                    }
                    prize.detalle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "peso ${prize.peso}  ·  %.2f%% de probabilidad"
                            .format(state.probabilidad(prize)),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (prize.activo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = prize.activo,
                    onCheckedChange = { viewModel.onActivarPremio(prize, it) },
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun PremioDialog(
    existente: WheelPrize?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (WheelPrizeDraft) -> Unit,
) {
    var rotulo by remember { mutableStateOf(existente?.rotulo ?: "") }
    var detalle by remember { mutableStateOf(existente?.detalle ?: "") }
    var peso by remember { mutableStateOf((existente?.peso ?: 1).toString()) }
    var orden by remember { mutableStateOf((existente?.orden ?: 0).toString()) }
    var otorga by remember { mutableStateOf(existente?.otorgaPremio ?: true) }

    // El rótulo se pinta girado dentro del sector: 12 caracteres es el tope
    val rotuloValido = rotulo.isNotBlank() && rotulo.length <= 12
    val valido = rotuloValido && peso.toIntOrNull()?.let { it >= 0 } == true &&
        orden.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existente == null) "Nuevo premio" else "Editar premio") },
        text = {
            Column {
                OutlinedTextField(
                    value = rotulo,
                    onValueChange = { rotulo = it },
                    label = { Text("Rótulo del sector") },
                    singleLine = true,
                    isError = rotulo.isNotBlank() && !rotuloValido,
                    supportingText = { Text("${rotulo.length}/12") },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = detalle,
                    onValueChange = { detalle = it },
                    label = { Text("Frase del resultado") },
                    supportingText = { Text("${detalle.length}/200") },
                    isError = detalle.length > 200,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it },
                        label = { Text("Peso") },
                        singleLine = true,
                        isError = peso.toIntOrNull() == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = orden,
                        onValueChange = { orden = it },
                        label = { Text("Orden") },
                        singleLine = true,
                        isError = orden.toIntOrNull() == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = otorga, onCheckedChange = { otorga = it })
                    Text("Otorga premio")
                }
                if (!otorga) {
                    Text(
                        "Sector de consolación: no se emite código para reclamar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !isSaving,
                onClick = {
                    onSave(
                        WheelPrizeDraft(
                            rotulo = rotulo.trim(),
                            detalle = detalle.trim().ifBlank { null },
                            peso = peso.toInt(),
                            otorgaPremio = otorga,
                            orden = orden.toInt(),
                        )
                    )
                },
            ) { Text(if (isSaving) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

// ============ Códigos ============

@Composable
private fun CodigosTab(state: WheelUiState, viewModel: WheelViewModel) {
    Column(Modifier.fillMaxSize()) {
        // Verificación puntual: el código llega dictado por WhatsApp
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.busqueda,
                onValueChange = viewModel::onBusqueda,
                label = { Text("Verificar código") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = viewModel::onVerificarCodigo,
                enabled = state.busqueda.isNotBlank() && !state.isSaving,
            ) { Text("Buscar") }
        }

        state.codigoBuscado?.let { spin ->
            ResultadoCodigo(spin) { viewModel.onCanjear(spin, !spin.canjeado) }
        }
        if (state.busquedaSinResultado) {
            Text(
                "No existe ningún código con ese valor.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.soloPendientes,
                onClick = { viewModel.onToggleSoloPendientes(true) },
                label = { Text("Por canjear") },
            )
            FilterChip(
                selected = !state.soloPendientes,
                onClick = { viewModel.onToggleSoloPendientes(false) },
                label = { Text("Todos") },
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        }

        if (state.spins.isEmpty()) {
            Centrado {
                Text(
                    if (state.soloPendientes) "No hay códigos por canjear"
                    else "Aún no se ha emitido ningún código",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                items(state.spins, key = { it.id }) { spin ->
                    FilaCodigo(spin) { viewModel.onCanjear(spin, !spin.canjeado) }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResultadoCodigo(spin: WheelSpin, onCanjear: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (spin.canjeado) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(spin.codigo, style = MaterialTheme.typography.titleMedium)
            Text(
                "Premio: ${spin.rotulo}  ·  emitido ${spin.createdAt?.take(10) ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            if (spin.canjeado) {
                Text(
                    "YA FUE CANJEADO el ${spin.canjeadoAt?.take(10) ?: "—"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
                OutlinedButton(onClick = onCanjear) { Text("Revertir canje") }
            } else {
                Text(
                    "Válido para canjear",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Button(onClick = onCanjear) { Text("Marcar como entregado") }
            }
        }
    }
}

@Composable
private fun FilaCodigo(spin: WheelSpin, onCanjear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(spin.codigo, style = MaterialTheme.typography.bodyLarge)
            Text(
                listOfNotNull(
                    spin.rotulo,
                    spin.createdAt?.take(10),
                    if (spin.canjeado) "canjeado" else null,
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = if (spin.canjeado) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
            )
        }
        TextButton(onClick = onCanjear) {
            Text(if (spin.canjeado) "Revertir" else "Entregar")
        }
    }
}

// ============ Bases ============

@Composable
private fun BasesTab(state: WheelUiState, viewModel: WheelViewModel) {
    LazyColumn {
        if (state.terminosPendientes > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${state.terminosPendientes} cláusula(s) con huecos sin " +
                                "rellenar. Revísalas antes de publicar la promoción.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }

        items(state.terms, key = { it.id }) { term ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onEditarTermino(term) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${term.orden}. ${term.titulo}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        term.cuerpo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                if (term.tienePendientes) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Tiene huecos por rellenar",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                if (!term.activo) {
                    Text(
                        "Oculta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun TerminoDialog(
    term: WheelTerm,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (WheelTerm) -> Unit,
) {
    var titulo by remember { mutableStateOf(term.titulo) }
    var cuerpo by remember { mutableStateOf(term.cuerpo) }
    var activo by remember { mutableStateOf(term.activo) }

    val valido = titulo.isNotBlank() && titulo.length <= 80 &&
        cuerpo.isNotBlank() && cuerpo.length <= 2000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar cláusula") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    isError = titulo.length > 80,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cuerpo,
                    onValueChange = { cuerpo = it },
                    label = { Text("Texto") },
                    minLines = 6,
                    supportingText = { Text("${cuerpo.length}/2000") },
                    isError = cuerpo.length > 2000,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activo, onCheckedChange = { activo = it })
                    Text("Visible en la web")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !isSaving,
                onClick = {
                    onSave(
                        term.copy(
                            titulo = titulo.trim(),
                            cuerpo = cuerpo.trim(),
                            activo = activo,
                        )
                    )
                },
            ) { Text(if (isSaving) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
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
