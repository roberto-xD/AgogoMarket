package com.passioagogo.market.presentation.view.screens

import android.widget.Toast
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.presentation.view.components.ProductCard
import com.passioagogo.market.presentation.viewModel.VM

@Composable
fun DashboardScreen(
    viewModel: VM = hiltViewModel()
){
    val product = viewModel.productData.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getProductData()
    }
    Scaffold(
        topBar = {
            Spacer(modifier = Modifier.size(30.dp))
        },
        content = { paddingValues ->
            LazyVerticalGrid(
                modifier = Modifier.padding(paddingValues).padding(top = 5.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(
                    product.value.size
                ){
                    product.value.get(it).let {
                        ProductCard(
                            id = it.id,
                            tittle = it.title,
                            urlImage = it.image,
                            onStock = it.isActive,
                            finalPrice = it.price?.price_og.orEmpty(),
                            originalPrice = it.price?.price_normal_og.orEmpty()
                        ) {
                            Toast.makeText(context,"on click: ${it}",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        },
        bottomBar = {

        },
        snackbarHost = {

        }
    )
}