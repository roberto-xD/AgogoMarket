package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.bean.EstructuraValidacion
import com.passioagogo.market.domain.bean.ProductoImport
import com.passioagogo.market.domain.bean.ResultadoImportacion
import com.passioagogo.market.presentation.viewModel.products.ConfiguracionImportacion
import com.passioagogo.market.presentation.viewModel.products.ImportacionViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportacionScreen(
    viewModel: ImportacionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var urlText by remember { mutableStateOf("") }

    // Manejar mensajes
    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let {
            delay(3000)
            viewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            delay(5000)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar desde Google Sheets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.limpiarResultados() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Sección 1: URL del Spreadsheet
            item {
                UrlInputSection(
                    url = urlText,
                    onUrlChange = {
                        urlText = it
                        viewModel.validarUrl(it)
                    },
                    validandoUrl = uiState.validandoUrl,
                    urlValida = uiState.urlValida,
                    mensajeValidacion = uiState.mensajeValidacion
                )
            }

            // Sección 2: Validación de estructura
            uiState.estructuraValidacion?.let { validacion ->
                item {
                    EstructuraValidacionCard(validacion = validacion)
                }
            }

            // Sección 3: Configuración de importación
            if (uiState.urlValida) {
                item {
                    ConfiguracionImportacionCard(
                        configuracion = uiState.configuracionImportacion,
                        onConfiguracionChange = viewModel::actualizarConfiguracion
                    )
                }
            }

            // Sección 4: Vista previa
            if (uiState.urlValida) {
                item {
                    VistaPreviaSection(
                        productos = uiState.vistaPreviaProductos,
                        cargando = uiState.cargandoVistaPrevia,
                        onCargarVistaPrevia = { viewModel.obtenerVistaPrevia(urlText) }
                    )
                }
            }

            // Sección 5: Botón de importación
            if (uiState.urlValida && uiState.vistaPreviaProductos.isNotEmpty()) {
                item {
                    ImportacionButton(
                        importando = uiState.importando,
                        progreso = uiState.progresoImportacion,
                        onImportar = {
                            with(uiState.configuracionImportacion) {
                                viewModel.iniciarImportacion(
                                    url = urlText,
                                    validarDuplicados = validarDuplicados,
                                    saltearDuplicados = saltearDuplicados,
                                    actualizarExistentes = actualizarExistentes
                                )
                            }
                        }
                    )
                }
            }

            // Sección 6: Resultado de importación
            uiState.resultadoImportacion?.let { resultado ->
                item {
                    ResultadoImportacionCard(resultado = resultado)
                }
            }

            // Mensajes de estado
            uiState.mensajeExito?.let { mensaje ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = mensaje)
                        }
                    }
                }
            }

            uiState.errorMessage?.let { mensaje ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mensaje,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrlInputSection(
    url: String,
    onUrlChange: (String) -> Unit,
    validandoUrl: Boolean,
    urlValida: Boolean,
    mensajeValidacion: String?
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "1. URL del Google Spreadsheet",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("URL del documento") },
                placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                trailingIcon = {
                    when {
                        validandoUrl -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        urlValida -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                        url.isNotBlank() && !urlValida -> Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                        else -> null
                    }
                },
                supportingText = {
                    Column {
                        Text("Pegue el enlace completo del Google Spreadsheet")
                        mensajeValidacion?.let { mensaje ->
                            Text(
                                text = mensaje,
                                color = if (urlValida) Color.Green else Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instrucciones
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📋 Instrucciones:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Haga el documento público o compartible\n" +
                                "• Asegúrese que la primera fila contenga los encabezados\n" +
                                "• Use el enlace de 'Compartir' del documento",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun EstructuraValidacionCard(validacion: EstructuraValidacion) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "2. Validación de Estructura",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Estado general
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (validacion.esValida) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (validacion.esValida) Color.Green else Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (validacion.esValida) "Estructura válida" else "Estructura inválida",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (validacion.esValida) Color.Green else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detalles
            Text("Total de filas: ${validacion.totalFilas}")
            Text("Headers encontrados: ${validacion.headersEncontrados.size}")

            if (validacion.headersFaltantes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "❌ Headers faltantes:",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                validacion.headersFaltantes.forEach { header ->
                    Text("• $header", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (validacion.erroresEstructura.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "❌ Errores de estructura:",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                validacion.erroresEstructura.forEach { error ->
                    Text("• $error", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

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

@Composable
fun VistaPreviaSection(
    productos: List<ProductoImport>,
    cargando: Boolean,
    onCargarVistaPrevia: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4. Vista Previa",
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(
                    onClick = onCargarVistaPrevia,
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Cargar Vista Previa")
                }
            }

            if (productos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Primeros ${productos.size} productos encontrados:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Tabla de vista previa
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text("SKU", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Nombre", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                            Text("Precio", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Stock", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Items
                    items(productos) { producto ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = producto.skuInterno,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = producto.nombre,
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${producto.precioVenta}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${producto.cantidadActual}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (producto != productos.last()) {
                            Divider(alpha = 0.3f)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportacionButton(
    importando: Boolean,
    progreso: Int,
    onImportar: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "5. Ejecutar Importación",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (importando) {
                Column {
                    LinearProgressIndicator(
                        progress = progreso / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Importando... $progreso%",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Button(
                    onClick = onImportar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar Importación")
                }
            }
        }
    }
}

@Composable
fun ResultadoImportacionCard(resultadoResultadoImportacion) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Resultado de Importación",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Estadísticas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    valor = resultado.exitosos,
                    etiqueta = "Exitosos",
                    color = Color.Green
                )
                EstadisticaItem(
                    valor = resultado.fallidos,
                    etiqueta = "Fallidos",
                    color = Color.Red
                )
                EstadisticaItem(
                    valor = resultado.duplicadosEncontrados,
                    etiqueta = "Duplicados",
                    color = Color.Orange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información adicional
            Text("Total procesados: ${resultado.totalProcesados}")
            Text("Tiempo transcurrido: ${resultado.tiempoTranscurrido / 1000}s")

            // Errores si existen
            if (resultado.errores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "❌ Errores encontrados:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(resultado.errores.take(10)) { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "Fila ${error.numeroFila} - SKU: ${error.sku}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = error.mensaje,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    if (resultado.errores.size > 10) {
                        item {
                            Text(
                                text = "... y ${resultado.errores.size - 10} errores más",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticaItem(
    valor: Int,
    etiqueta: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall
        )
    }
}