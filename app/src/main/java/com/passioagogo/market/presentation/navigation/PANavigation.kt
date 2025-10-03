package com.passioagogo.market.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.presentation.view.screens.DashboardScreen
import com.passioagogo.market.presentation.view.screens.ImageGalleryScreen
import com.passioagogo.market.presentation.view.screens.ImportacionScreen
import com.passioagogo.market.presentation.view.screens.ItemScreen
import com.passioagogo.market.presentation.view.screens.SearchScreen
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel


@Composable
fun PANavigation(
    startDestination: String,
    imageViewModel: ImageGalleryViewModel,
    detalleViewModel: DetalleProductoViewModel,
){
    val navController = rememberNavController()

    val uiImageState = imageViewModel.uiState.collectAsState()
    val imagePaths = uiImageState.value.images

    LaunchedEffect(imagePaths) {
        if(imagePaths.isNotEmpty() && navController.currentDestination?.route != Routes.ItemDetail.Name ){
            navController.navigate(Routes.ItemDetail.Name)
        }
    }

    fun onBack(){
        navController.popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable(
            route = Routes.Dashboard.Name
        ) {
            DashboardScreen(
                detalleViewModel = detalleViewModel,
                navigateToProductScreen = {
                    navController.navigate(Routes.ItemDetail.Name)
                },
                navigate = {
                    navController.navigate(it)
                }
            )
        }

        composable(
            route = Routes.ItemDetail.Name,

        ){ backStackEntry ->
            ItemScreen(
                detalleViewModel = detalleViewModel,
                imageViewModel = imageViewModel,
                onBackClick = ::onBack
            )
        }
        composable(
            route = Routes.Login.Name
        ) {

        }

        composable(
            route = Routes.ImageGallery.Name
        ) {
            ImageGalleryScreen(
                viewModel = imageViewModel,
                onBackClick = ::onBack
            )
        }

        composable(
            route = Routes.Search.Name
        ) {
            SearchScreen{
                navController.navigate(it)
            }
        }
        composable(
            route = Routes.Import.Name
        ) {
            ImportacionScreen(
                onNavigateBack = ::onBack
            )
        }
    }
}