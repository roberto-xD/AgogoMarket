package com.passioagogo.market.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.passioagogo.market.ui.requests.RequestCartScreen
import com.passioagogo.market.ui.requests.RequestDetailScreen
import com.passioagogo.market.ui.requests.RequestsListScreen

internal fun NavGraphBuilder.NavigationHostClient(
    navController: NavHostController,
    onBack: ()-> Unit,
){
    composable<NavigationRoutes.ShoppingCart>{
        RequestCartScreen(
            onBack = onBack
        )
    }
    composable<NavigationRoutes.RequestList>{
        RequestsListScreen(
            onBack = onBack,
            onOpenRequest = {
                navController.navigate(NavigationRoutes.Request(it))
            },
        )
    }
    composable<NavigationRoutes.Request>{
        RequestDetailScreen(onBack = onBack)
    }
}