package com.passioagogo.market.presentation.view.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.passioagogo.market.R
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import com.passioagogo.market.presentation.view.components.BarcodeScannerScreen
import com.passioagogo.market.presentation.view.components.containers.PABottomSheetContainer
import com.passioagogo.market.presentation.view.components.PAImageItem
import com.passioagogo.market.presentation.view.components.PAToolbar
import com.passioagogo.market.presentation.view.components.skeleton.ProductFormSkeleton
import com.passioagogo.market.presentation.view.templates.PAInfoProduct
import com.passioagogo.market.presentation.viewModel.BackPressHandler
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.presentation.viewModel.products.DashboardViewModel
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel
import com.passioagogo.market.ui.decorators.orZero
import com.passioagogo.market.ui.utils.PAConstants.NEW_CATEGORY
import com.passioagogo.market.ui.utils.PAConstants.NEW_SUBCATEGORY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    imageViewModel: ImageGalleryViewModel,
    detalleViewModel: DetalleProductoViewModel,
    dashboardViewModel: DashboardViewModel,
    navigateToBack: () -> Unit,
){
    val context = LocalContext.current
    val showBottomSheet = imageViewModel.showBottomSheet

    val uiImageState = imageViewModel.uiState.collectAsState()
    val imagePaths = uiImageState.value.images
    val deleteImage = uiImageState.value.deleteImage

    val catalogoUiState = dashboardViewModel.uiState.collectAsState().value

    val uiProductState = detalleViewModel.uiState.collectAsState()
    val currentProduct = uiProductState.value.productoDetallado

    val showScanner = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val show = if(uris.size < 5) uris else uris.subList(0,5)
            imageViewModel.saveSharedImages(show, currentProduct.producto.id)
        }
    }

    fun openPicker(){
        try {
            filePickerLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
        } catch (e2: Exception){

        }
    }

    val scope = rememberCoroutineScope()
    val backPressHandler = remember { BackPressHandler(delayMillis = 4000L) }

    fun backClick(){
        backPressHandler.onBackPressed(
            scope = scope,
            onFirstPress = {
                Toast.makeText(context, "Presiona de nuevo para salir", Toast.LENGTH_LONG).show()
            },
            onSecondPress = {
                imageViewModel.clearPaths()
                detalleViewModel.limpiarMensajes()
                navigateToBack()
            }
        )
    }


    // Limpiar al salir
    DisposableEffect(Unit) {
        onDispose {
            backPressHandler.reset()
        }
    }

    BackHandler {
        backClick()
    }

    LaunchedEffect(imagePaths) {
        detalleViewModel.actualizarItem(
            productoDetallado = currentProduct.copy(
                imagenes = currentProduct.imagenes + imagePaths,
            )
        )
        showBottomSheet.value = imagePaths.isNotEmpty()
    }
    LaunchedEffect(deleteImage) {
        deleteImage?.let {
            detalleViewModel.actualizarItem(
                productoDetallado = currentProduct.copy(
                    imagenes = currentProduct.imagenes.filter { it.rutaImagen != deleteImage },
                )
            )
        }
    }

    LaunchedEffect(uiProductState.value.errorMessage) {
        uiProductState.value.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    LaunchedEffect(uiProductState.value.mensajeExito) {
        uiProductState.value.mensajeExito?.let { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    if(uiProductState.value.isLoading){
        ProductFormSkeleton()
    }else {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceBright),
            topBar = {
                PAToolbar(
                    centerText = if(currentProduct.producto.id.orZero() == 0L){stringResource(R.string.label_new_item)} else{stringResource(R.string.label_update_item)},
                    leftIcon = R.drawable.arrow_back,
                    onLeftClick = {
                        backClick()
                    },
                    rightText = if(currentProduct.producto.id.orZero() == 0L){"Guardar"} else{"Actualizar"},
                    onRightClick = {
                        Log.i(TAG_PG,"onSaveClick")
                        if(currentProduct.validateRules()){
                            val data = currentProduct.toActualizaProductoParams()
                            Log.i(TAG_PG,"$data")
                            if(data.id != 0L){
                                Log.i(TAG_PG,"actualizar")
                                detalleViewModel.actualizarProductoDetallado(data)
                            }else {
                                Log.i(TAG_PG,"crear")
                                detalleViewModel.crearProductoDetallado(data)
                            }
                        } else {
                            Toast
                                .makeText(context, "Rellene todos los campos", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(5.dp)
                    .fillMaxSize()
            ) {
                PAInfoProduct(
                    initialData = currentProduct,
                    catalogData = catalogoUiState,
                    createNew = {
                        when(it.first){
                            NEW_CATEGORY -> {
                                dashboardViewModel.crearCategoriaConFamilia(
                                    nombre = it.second,
                                    descripcion = "",
                                    familiaId = currentProduct.idFamilia
                                )
                            }
                            NEW_SUBCATEGORY -> {
                                dashboardViewModel.crearSubcategoriaConCategoria(
                                    nombre = it.second,
                                    descripcion = "",
                                    categoriaId = currentProduct.idCategoria
                                )
                            }
                        }
                    },
                    onImageClick = {
                        if(currentProduct.imagenes.isEmpty()){
                            openPicker()
                        } else {
                            showBottomSheet.value = true
                        }
                    },
                    onScanClick = {
                        showScanner.value = true
                        Toast
                            .makeText(context, "abrir escaner", Toast.LENGTH_SHORT)
                            .show()
                    },
                ){ updatedProduct ->
                    if(updatedProduct.idFamilia != currentProduct.idFamilia){
                        dashboardViewModel.obtenerCategoriasPorFamiliaId(updatedProduct.idFamilia)
                    }
                    if(updatedProduct.idCategoria != currentProduct.idCategoria){
                        dashboardViewModel.obtenerSubcategoriasPorCategoria(updatedProduct.idCategoria)
                    }
                    detalleViewModel.actualizarItem(updatedProduct)

                }
            }
            PABottomSheetContainer(
                showBottomSheet = showBottomSheet
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = currentProduct.imagenes.size,
                    ){ item ->
                        PAImageItem(
                            imagePath = currentProduct.imagenes[item].rutaImagen,
                            onDeleteClick = {
                                imageViewModel.deleteImage(currentProduct.imagenes[item])
                            }
                        )
                    }
                    item{
                        PAImageItem(
                            onImageClick = {
                                if(currentProduct.imagenes.size < 5){
                                    openPicker()
                                } else{
                                    Toast
                                        .makeText(context, "Máximo 5 imágenes", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        )
                    }
                }
            }

            PABottomSheetContainer(
                showBottomSheet = showScanner,
                showFullScreen = false,
            ) {
                BarcodeScannerScreen(
                    onBarcodeScanned = { code ->
                        showScanner.value = false
                        detalleViewModel.actualizarItem(
                            productoDetallado = currentProduct.copy(
                                producto = currentProduct.producto.copy(
                                    codigoBarras = code
                                )
                            )
                        )
                        Log.i(TAG_PG, "Código escaneado: $code")
                    }
                )
            }


        }
    }
}