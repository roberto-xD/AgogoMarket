package com.passioagogo.market.presentation.view.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import com.passioagogo.market.presentation.navigation.Routes
import com.passioagogo.market.presentation.view.components.PADrawerSheet
import com.passioagogo.market.presentation.view.components.ProductCard
import com.passioagogo.market.presentation.view.components.SearchInput
import com.passioagogo.market.presentation.view.components.Splash
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel
import com.passioagogo.market.presentation.viewModel.products.DashboardViewModel
import com.passioagogo.market.ui.decorators.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    detalleViewModel: DetalleProductoViewModel,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    navigateToProductScreen: () -> Unit,
    navigate: (route: String) -> Unit,
) {
    val context = LocalContext.current
    val product = dashboardViewModel.uiState.collectAsState()

    val columstate = rememberLazyGridState()
    val isAtBottom = columstate.canScrollForward.not()

    val searchState = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            Log.i(TAG_PG, "load more data...")
        }
    }

    LaunchedEffect(Unit) {
        dashboardViewModel.buscarProductos("")
    }

    PADrawerSheet(
        navigate = navigate,
        toolbarCenter = {
            searchState.value = searchState.value.not()
            if(searchState.value.not()){
                dashboardViewModel.buscarProductos("")
            }
        }
    ) {     padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            if (searchState.value) {
                SearchInput {
                    Log.i(TAG_PG,"keyboard search press: $it")
                    dashboardViewModel.buscarProductos(it)
                }
            }
            if (product.value.productos.size != 0) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(top = 5.dp),
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    state = columstate
                ) {
                    items(
                        product.value.productos.size
                    ) {
                        product.value.productos.get(it).let { product ->
                            ProductCard(
                                id = product.id,
                                tittle = product.nombre,
                                imagePath = product.imagenPrincipal,
                                sellPrice = product.precioVenta.toString(),
                            ) { id ->
                                detalleViewModel.cargarProducto(id)
                                navigateToProductScreen()
                                Toast.makeText(context, "id: $id", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    if (product.value.isLoading) {
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
            } else {
                Splash()
            }
        }

//        PABottomSheetContainer(
//            showBottomSheet = showBottomSheet
//        ){
//            DetailBottomSheet(
//                enableEdit = isEditor.value,
//                currentProduct = currentProduct.value
//            ) { updateList ->
//                if (updateList) {
//                    isEditor.value = false
//                    productViewModel.buscarProductos("")
//                }
//            }
//        }
    }

}

@Composable
@Preview
private fun Preview() {

}