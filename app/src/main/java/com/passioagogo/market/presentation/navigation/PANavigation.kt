package com.passioagogo.market.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.presentation.view.screens.DashboardScreen
import com.passioagogo.market.presentation.view.screens.LoginScreen


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
            DashboardScreen()
        }
        composable(
            route = Routes.Login.Name
        ) {
            LoginScreen()
        }
    }
}