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
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.R
import com.passioagogo.market.presentation.view.components.BarcodeScannerScreen
import com.passioagogo.market.presentation.view.components.PABottomSheetContainer
import com.passioagogo.market.presentation.view.components.PAImageItem
import com.passioagogo.market.presentation.view.components.PAToolbar
import com.passioagogo.market.presentation.view.components.skeleton.ProductFormSkeleton
import com.passioagogo.market.presentation.view.models.PAInfoModel
import com.passioagogo.market.presentation.view.templates.PAInfoProduct
import com.passioagogo.market.presentation.viewModel.BackPressHandler
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.presentation.viewModel.products.DashboardViewModel
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel
import com.passioagogo.market.presentation.viewModel.products.FamiliasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    imageViewModel: ImageGalleryViewModel,
    detalleViewModel: DetalleProductoViewModel,
    familiasViewModel: FamiliasViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    navigateToBack: () -> Unit,
){
    val context = LocalContext.current
    val showBottomSheet = imageViewModel.showBottomSheet

    val uiImageState = imageViewModel.uiState.collectAsState()
    val imagePaths = uiImageState.value.images
    val deleteImage = uiImageState.value.deleteImage

    val familiaState = familiasViewModel.familias.collectAsState()
    val familias = familiaState.value.map { it.descripcion }.toMutableList()

    val productosState = dashboardViewModel.uiState.collectAsState()
    val categorias = productosState.value.categorias.map { it.nombre }.toMutableList()
    val proveedores = productosState.value.proveedores.map { it.nombre }.toMutableList()

    val uiProductState = detalleViewModel.uiState.collectAsState()
    val producto = uiProductState.value.productoDetallado

    val currentProduct = remember { mutableStateOf(PAInfoModel()) }
    val showScanner = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = detalleViewModel.snackbarMessage.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val show = if(uris.size < 5) uris else uris.subList(0,5)
            imageViewModel.saveSharedImages(show, currentProduct.value.id)
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
                currentProduct.value = PAInfoModel()
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

    LaunchedEffect(producto) {
        producto?.let { prod ->
            currentProduct.value = currentProduct.value.update(prod)
        }

    }
    LaunchedEffect(familias) {
        currentProduct.value = currentProduct.value.copy(
            familyList = familias
        )
    }
    LaunchedEffect(categorias) {
        Log.i("tag_pg","categorias: $categorias")
        currentProduct.value = currentProduct.value.copy(
            categoryList = categorias
        )
    }
    LaunchedEffect(imagePaths) {
        currentProduct.value = currentProduct.value.copy(
            pathImageList = currentProduct.value.pathImageList + imagePaths
        )
        showBottomSheet.value = currentProduct.value.pathImageList.isNotEmpty()
    }
    LaunchedEffect(deleteImage) {
        deleteImage?.let {
            currentProduct.value = currentProduct.value.copy(
                pathImageList = currentProduct.value.pathImageList.filter { it!=deleteImage }
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
                    leftIcon = R.drawable.arrow_back,
                    onLeftClick = {
                        backClick()
                    },
                    centerText = stringResource(R.string.label_item),
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
                    initialData = currentProduct.value,
                    onImageClick = {
                        if(currentProduct.value.pathImageList.isEmpty()){
                            openPicker()
                        } else {
                            showBottomSheet.value = true
                        }
                    },
                    onFamilyDropDownSelect = {
                        dashboardViewModel.obtenerCategoriasPorFamilia(it)
                    },
                    onScanClick = {
                        showScanner.value = true
                        Toast
                            .makeText(context, "abrir escaner", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onSaveClick = {
                        val data = currentProduct.value.toActualizaProductoParams()
                        Log.i("tag","onSaveClick: $data")
                        if(data.id != 0L){
                            detalleViewModel.actualizarProductoDetallado(data)
                        }else {
                            detalleViewModel.crearProductoDetallado(data)
                        }
                    },
                ){ updatedProduct ->
                    currentProduct.value = updatedProduct
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
                        count = currentProduct.value.pathImageList.size,
                    ){ item ->
                        PAImageItem(
                            imagePath = currentProduct.value.pathImageList[item],
                            onDeleteClick = {
                                imageViewModel.deleteImage(currentProduct.value.pathImageList[item])
                            }
                        )
                    }
                    item{
                        PAImageItem(
                            onImageClick = {
                                if(currentProduct.value.pathImageList.size < 5){
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
                showFullScreen = true,
            ) {
                BarcodeScannerScreen(
                    onBarcodeScanned = { code ->
                        showScanner.value = false
                        currentProduct.value = currentProduct.value.copy(
                            codigoBarra = code
                        )
                        Log.i("tag_pg", "Código escaneado: $code")
                    }
                )
            }
        }
    }
}