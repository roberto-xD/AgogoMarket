package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.presentation.viewModel.VM

@Composable
fun DashboardScreen(
    viewModel: VM = hiltViewModel()
){
    val product = viewModel.productData.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.getProductData()
    }
    Scaffold(
        topBar = {

        },
        content = { paddingValues ->
            Text(
                text = "Hello !",
                modifier = Modifier.padding(paddingValues)
            )
            Text(
                text = product.value.toString(),
                modifier = Modifier.padding(paddingValues)
            )
        },
        bottomBar = {

        },
        snackbarHost = {

        }
    )
}