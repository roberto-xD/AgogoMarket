package com.passioagogo.market.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.presentation.view.screens.DashboardScreen
import com.passioagogo.market.presentation.view.screens.ImageGalleryScreen
import com.passioagogo.market.presentation.view.screens.ImportacionScreen
import com.passioagogo.market.presentation.view.screens.ItemScreen
import com.passioagogo.market.presentation.view.screens.SearchScreen
import com.passioagogo.market.presentation.viewModel.imagenes.ImageGalleryViewModel


@Composable
fun PANavigation(
    startDestination: String,
    imageGalleryViewModel: ImageGalleryViewModel,
){
    val navController = rememberNavController()

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
            DashboardScreen{
                navController.navigate(it)
            }
        }
        composable(
            route = Routes.ItemDetail.Name
        ){
            ItemScreen(
                imageViewModel = imageGalleryViewModel,
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
                viewModel = imageGalleryViewModel,
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