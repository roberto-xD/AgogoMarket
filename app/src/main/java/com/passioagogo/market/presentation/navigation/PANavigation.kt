package com.passioagogo.market.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.presentation.view.screens.DashboardScreen
import com.passioagogo.market.presentation.view.screens.LoginScreen
import com.passioagogo.market.presentation.view.screens.SearchScreen


@Composable
fun PANavigation(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.Name
    ){
        composable(
            route = Routes.Dashboard.Name
        ) {
            DashboardScreen{
                navController.navigate(it)
            }
        }
        composable(
            route = Routes.Login.Name
        ) {
            LoginScreen()
        }
        composable(
            route = Routes.Search.Name
        ) {
            SearchScreen{
                navController.navigate(it)
            }
        }
    }
}