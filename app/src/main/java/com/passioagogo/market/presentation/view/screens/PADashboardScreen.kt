package com.passioagogo.market.presentation.view.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.view.components.DetailBottomSheet
import com.passioagogo.market.presentation.view.components.Loading
import com.passioagogo.market.presentation.view.components.ProductCard
import com.passioagogo.market.presentation.viewModel.VM
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: VM = hiltViewModel()
){
    val context = LocalContext.current
    val product = viewModel.productData.collectAsState()
    val isEditor = viewModel.isEditor.collectAsState()
    val currentProduct = remember {
        mutableStateOf(PAProductBean())
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.getProductData()
    }
    Scaffold(
        topBar = {
            if(product.value.size != 0){
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 5.dp)
                            .combinedClickable(
                                onLongClick = {
                                    viewModel.isEditor.value = viewModel.isEditor.value.not()
                                    Toast
                                        .makeText(
                                            context, if (viewModel.isEditor.value) {
                                                "Modo Editor"
                                            } else {
                                                "Modo Observador"
                                            }, Toast.LENGTH_SHORT
                                        )
                                        .show()
                                },
                                onClick = {}
                            ),
                        painter = painterResource(
                            id = R.drawable.branding_passion_20),
                        colorFilter = ColorFilter.tint(Color.Black),
                        contentDescription = null,
                    )
                }
            }
        },
        content = { paddingValues ->
            if(product.value.size != 0){
                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(top = 5.dp),
                    columns = GridCells.Adaptive(minSize = 128.dp)
                ) {
                    items(
                        product.value.size
                    ){
                        product.value.get(it).let { product ->
                            ProductCard(
                                id = product.id,
                                tittle = product.title,
                                urlImage = product.image,
                                onStock = product.isActive,
                                finalPrice = product.price?.price_og.orEmpty(),
                                originalPrice = product.price?.price_normal_og.orEmpty()
                            ) {
                                currentProduct.value = product
                                showBottomSheet.value = true
                            }
                        }
                    }
                }
            }else{
                Loading()
            }

            if(showBottomSheet.value){
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet.value = false },
                    sheetState = sheetState
                ) {
                    DetailBottomSheet(
                        enableEdit = isEditor.value,
                        currentProduct = currentProduct.value
                    ){
                        if(it){
                            viewModel.isEditor.value = false
                            viewModel.getProductData()
                        }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if(!sheetState.isVisible){
                                showBottomSheet.value = false
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
@Preview
private fun Preview(){

}