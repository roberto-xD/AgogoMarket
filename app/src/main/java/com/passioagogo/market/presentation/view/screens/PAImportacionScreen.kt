package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.presentation.view.components.items.ConfiguracionImportacionCard
import com.passioagogo.market.presentation.view.components.items.EstructuraValidacionCard
import com.passioagogo.market.presentation.view.components.items.ImportacionButton
import com.passioagogo.market.presentation.view.components.items.ResultadoImportacionCard
import com.passioagogo.market.presentation.view.components.items.UrlInputSection
import com.passioagogo.market.presentation.view.components.items.VistaPreviaSection
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
                                Icons.Default.AccountBox,
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













