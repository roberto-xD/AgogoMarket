package com.passioagogo.market.presentation.view.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.R
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.presentation.view.components.DetailBottomSheet
import com.passioagogo.market.presentation.view.components.DrawerSheet
import com.passioagogo.market.presentation.view.components.Loading
import com.passioagogo.market.presentation.view.components.ProductCard
import com.passioagogo.market.presentation.view.components.Toolbar
import com.passioagogo.market.presentation.viewModel.VM
import com.passioagogo.market.ui.decorators.shimmerEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VM = hiltViewModel(),
    navigate: (route: String) -> Unit,
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
    val columstate = rememberLazyGridState()
    val isAtBottom = columstate.canScrollForward.not()
    val loader = viewModel.loader.collectAsState()



    LaunchedEffect(isAtBottom) {
        if(isAtBottom){
            Log.i("tag","load more data...")
            viewModel.getProductData(startAfterLast = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getProductData()
    }

    DrawerSheet(
        addItem = {
            showBottomSheet.value = true
            currentProduct.value = PAProductBean()
        },
        navigate = navigate
    ) {
        if(product.value.size != 0){
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(it)
                    .padding(top = 5.dp),
                columns = GridCells.Adaptive(minSize = 128.dp),
                state = columstate
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
                            finalPrice = product.price.price_og,
                            originalPrice = product.price.price_normal_og
                        ) {
                            currentProduct.value = product
                            showBottomSheet.value = true
                        }
                    }
                }
                if(loader.value){
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .shimmerEffect()
                        )
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

}

@Composable
@Preview
private fun Preview(){

}