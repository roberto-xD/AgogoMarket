package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.R
import com.passioagogo.market.presentation.view.components.ClienteSection
import com.passioagogo.market.presentation.view.components.FechaActualSection
import com.passioagogo.market.presentation.view.components.PAContainer
import com.passioagogo.market.presentation.view.components.PADropDown
import com.passioagogo.market.presentation.view.components.ProductosSection
import com.passioagogo.market.presentation.view.components.TotalSection
import com.passioagogo.market.presentation.viewModel.ventas.RegistrarVentaViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarVentaScreen(
    viewModel: RegistrarVentaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSeleccionProductos: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val paymentMethods = listOf("Efectivo","Tarjeta","Transferencia","Mixto")

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.limpiarError()
        }
    }

    LaunchedEffect(uiState.ventaGuardada) {
        if (uiState.ventaGuardada) {
            snackbarHostState.showSnackbar("Venta registrada exitosamente")
            delay(500)
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    FechaActualSection(
                        fechaCreacion = uiState.fechaCreacion,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Sección Productos
            ProductosSection(
                productos = uiState.productos,
                onAgregarProducto = onNavigateToSeleccionProductos,
                onEliminarProducto = viewModel::eliminarProducto,
                onCantidadChange = viewModel::actualizarCantidadProducto,
                errorStock = uiState.errorStock
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Método de Pago
            Card {
                PADropDown(
                    item = uiState.metodoPago,
                    items = paymentMethods,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    placeHolder = stringResource(id = R.string.label_payment_method),
                ){
                    viewModel.onMetodoPagoChange(it)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Sección Cliente
            PAContainer(
                modifier = Modifier,
                isOpen = false,
                containerTittle = "Información del Cliente",
            ) {
                ClienteSection(
                    clienteModel = uiState.clienteModel,
                ){
                    viewModel.onClienteChange(it)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Total
            TotalSection(total = uiState.total)
            Spacer(modifier = Modifier.height(16.dp))

            // Botón Guardar
            Button(
                onClick = viewModel::guardarVenta,
                enabled = !uiState.isLoading && uiState.productos.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar Venta")
                }
            }
        }
    }
}