package com.passioagogo.market.presentation.view.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.passioagogo.market.presentation.viewModel.PABaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun DashboardScreen(
    viewModel: PABaseViewModel = hiltViewModel()
){
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
        },
        bottomBar = {

        },
        snackbarHost = {

        }
    )
}