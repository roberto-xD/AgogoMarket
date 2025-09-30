package com.passioagogo.market.presentation.view.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.R
import com.passioagogo.market.presentation.view.components.PABottomSheetContainer
import com.passioagogo.market.presentation.view.components.PAImageItem
import com.passioagogo.market.presentation.view.templates.PAInfoProduct
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    imageViewModel: ImageGalleryViewModel = hiltViewModel(),
    detalleViewModel: DetalleProductoViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
){
    val uiProductState = detalleViewModel.uiState.collectAsState()
    val producto = uiProductState.value.productoDetallado?.producto

    val context = LocalContext.current
    val showBottomSheet = remember { mutableStateOf(false) }

    val uiImageState = imageViewModel.uiState.collectAsState()
    val imagePaths = uiImageState.value.images
    val imagenPrincipal = remember { mutableStateListOf<String>() }

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

    LaunchedEffect(imagePaths) {
        if(imagePaths.isNotEmpty()){
            imagenPrincipal.addAll(imagePaths)
            showBottomSheet.value = true
        }else{
            imagenPrincipal.removeAll(imagenPrincipal)
        }
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceBright),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.label_item),
                        fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                rutasImagenList = imagenPrincipal,
                onImageClick = {
                    if(imagePaths.isEmpty()){
                        openPicker()
                    } else {
                        showBottomSheet.value = true
                    }
                },
                onSaveClick = {
                    Log.i("tag","onSaveClick: $it")
                },
                onScanClick = {
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
                            openPicker()
                        }
                    )
                }
            }
        }
    }


}