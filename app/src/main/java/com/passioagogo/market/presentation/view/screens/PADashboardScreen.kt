package com.passioagogo.market.presentation.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.presentation.view.components.DetailBottomSheet
import com.passioagogo.market.presentation.view.components.DrawerSheet
import com.passioagogo.market.presentation.view.components.Splash
import com.passioagogo.market.presentation.view.components.ProductCard
import com.passioagogo.market.presentation.view.components.SearchInput
import com.passioagogo.market.presentation.viewModel.products.ProductosViewModel
import com.passioagogo.market.presentation.viewModel.VM
import com.passioagogo.market.ui.decorators.shimmerEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    productViewModel: ProductosViewModel = hiltViewModel(),
    navigate: (route: String) -> Unit,
) {
    val product = productViewModel.uiState.collectAsState()
    val isEditor = remember { mutableStateOf(false) }
    val currentProduct = remember {
        mutableStateOf(Producto())
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = remember {
        mutableStateOf(false)
    }
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
        productViewModel.buscarProductos("")
    }

    DrawerSheet(
        addItem = {
            showBottomSheet.value = true
            currentProduct.value = Producto()
        },
        navigate = navigate,
        search = {
            searchState.value = searchState.value.not()
            if(searchState.value.not()){
                productViewModel.buscarProductos("")
            }
        }
    ) {     padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            if (searchState.value) {
                SearchInput {
                    Log.i(TAG_PG,"keyboard search press: $it")
                    productViewModel.buscarProductos(it)
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
                                id = product.id.toString(),
                                tittle = product.nombre,
                                urlImage = "",
                                onStock = product.activo,
                                finalPrice = product.precioVenta.toString(),
                                originalPrice = product.precioCompra.toString()
                            ) {
                                currentProduct.value = product
                                showBottomSheet.value = true
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

        if (showBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet.value = false },
                sheetState = sheetState
            ) {
                DetailBottomSheet(
                    enableEdit = isEditor.value,
                    currentProduct = currentProduct.value
                ) { updateList ->
                    if (updateList) {
                        isEditor.value = false
                        productViewModel.buscarProductos("")
                    }
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
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
private fun Preview() {

}