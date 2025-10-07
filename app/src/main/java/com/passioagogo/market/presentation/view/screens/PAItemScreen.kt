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
import com.passioagogo.market.presentation.view.templates.PAInfoProduct
import com.passioagogo.market.presentation.viewModel.BackPressHandler
import com.passioagogo.market.presentation.viewModel.ItemViewModel
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel
import com.passioagogo.market.presentation.viewModel.products.FamiliasViewModel
import com.passioagogo.market.presentation.viewModel.products.ProductosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    imageViewModel: ImageGalleryViewModel,
    detalleViewModel: DetalleProductoViewModel,
    itemViewModel: ItemViewModel = hiltViewModel(),
    familiasViewModel: FamiliasViewModel = hiltViewModel(),
    productosViewModel: ProductosViewModel = hiltViewModel(),
    navigateToBack: () -> Unit,
){
    val context = LocalContext.current
    val showBottomSheet = imageViewModel.showBottomSheet

    val uiImageState = imageViewModel.uiState.collectAsState()
    val imagePaths = uiImageState.value.images

    val familiaState = familiasViewModel.familias.collectAsState()
    val familias = familiaState.value.map { it.descripcion }.toMutableList()

    val productosState = productosViewModel.uiState.collectAsState()
    val categorias = productosState.value.categorias.map { it.nombre }.toMutableList()
    val proveedores = productosState.value.proveedores.map { it.nombre }.toMutableList()

    val uiProductState = detalleViewModel.uiState.collectAsState()
    val producto = uiProductState.value.productoDetallado

    val currentProduct = itemViewModel.uiState
    val showScanner = remember { mutableStateOf(false) }
    val count = remember { mutableStateOf(0) }


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            imageViewModel.saveSharedImages(uris)
        }
    }

    fun openPicker(){
        try {
            filePickerLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
        } catch (e2: Exception){

        }
    }

    val scope = rememberCoroutineScope()
    val backPressHandler = remember { BackPressHandler(delayMillis = 2000L) }

    fun backClick(){
        backPressHandler.onBackPressed(
            scope = scope,
            onFirstPress = {
                Toast.makeText(context, "Presiona de nuevo para salir", Toast.LENGTH_SHORT).show()
            },
            onSecondPress = {
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
            itemViewModel.setProductData(prod)
        }

    }
    LaunchedEffect(familias) {
        itemViewModel.setFamilyList(familias)
    }
    LaunchedEffect(categorias) {
        Log.i("tag_pg","categorias: $categorias")
        itemViewModel.setCategoryList(categorias)
    }
    LaunchedEffect(imagePaths) {
        itemViewModel.setImagePaths(imagePaths.toMutableList())
        showBottomSheet.value = imagePaths.isNotEmpty()
    }

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
                onImageClick = {
                    if(imagePaths.isEmpty()){
                        openPicker()
                    } else {
                        showBottomSheet.value = true
                    }
                },
                onFamilyDropDownSelect = {
                    productosViewModel.obtenerCategoriasPorFamilia(it)
                },
                onSaveClick = {
                    Log.i("tag","onSaveClick")
                },
                onScanClick = {
                    showScanner.value = true
                    Toast
                        .makeText(context, "abrir escaner", Toast.LENGTH_SHORT)
                        .show()
                }
            )
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
                    count = imagePaths.size,
                ){ item ->
                    PAImageItem(
                        imagePath = imagePaths[item],
                        onDeleteClick = {
                            imageViewModel.deleteImage(imagePaths[item])
                        }
                    )
                }
                item{
                    PAImageItem(
                        onImageClick = {
                            if(imagePaths.size < 5){
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
                    itemViewModel.setBarCode(code)
                    Log.i("tag_pg", "Código escaneado: $code")
                }
            )
        }
    }
}