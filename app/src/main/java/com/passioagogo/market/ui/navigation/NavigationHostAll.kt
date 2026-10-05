package com.passioagogo.market.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.passioagogo.market.ui.admin.catalog.CatalogAdminScreen
import com.passioagogo.market.ui.admin.promotions.PromotionEditScreen
import com.passioagogo.market.ui.admin.promotions.PromotionsListScreen
import com.passioagogo.market.ui.catalog.browse.ProductDetailScreen
import com.passioagogo.market.ui.requests.RequestsListScreen

internal fun NavGraphBuilder.NavigationHostAll(
    navController: NavHostController,
    onBack: () -> Unit,
){
    composable<NavigationRoutes.MainScreen>{

    }
    composable<NavigationRoutes.Catalog>{
        CatalogAdminScreen(
            onBack = onBack,
            onOpenProduct = { id -> navController.navigate(NavigationRoutes.EditProduct(id)) },
            onNewProduct = { navController.navigate(NavigationRoutes.EditProduct()) },
            onViewProduct = { id -> navController.navigate(NavigationRoutes.ProductDetail(id)) },
        )
    }
    composable<NavigationRoutes.ProductDetail>{
        ProductDetailScreen(onBack = onBack)
    }
    composable<NavigationRoutes.PromotionList> {
        PromotionsListScreen(
            onBack = onBack,
            onOpenPromotion = { id -> navController.navigate(NavigationRoutes.NewPromotion(id)) },
            onNewPromotion = { navController.navigate(NavigationRoutes.NewPromotion()) },
        )
    }
    composable<NavigationRoutes.NewPromotion>{
        PromotionEditScreen(
            onBack = onBack,
            onSaved = { navController.popBackStack() }
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
}