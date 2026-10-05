package com.passioagogo.market.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.passioagogo.market.ui.inventory.InventoryHomeScreen
import com.passioagogo.market.ui.inventory.requests.CreateTransferRequestScreen
import com.passioagogo.market.ui.inventory.requests.TransferRequestDetailScreen
import com.passioagogo.market.ui.inventory.transfers.CreateTransferScreen
import com.passioagogo.market.ui.inventory.transfers.TransferDetailScreen
import com.passioagogo.market.ui.orders.OrderDetailScreen
import com.passioagogo.market.ui.orders.OrdersListScreen
import com.passioagogo.market.ui.orders.shipping.CreateShippingScreen
import com.passioagogo.market.ui.pos.PosScreen

internal fun NavGraphBuilder.NavigationHostStaff(
    navController: NavHostController,
    onBack: ()-> Unit,
) {
    composable<NavigationRoutes.PointOfSale> {
        PosScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.InventoryHome> {
        InventoryHomeScreen(
            onOpenTransfer = { id -> NavigationRoutes.InventoryTransferDetail(id) },
            onCreateTransfer = { navController.navigate(NavigationRoutes.InventoryNewTransfer) },
            onOpenStockTake = { navController.navigate(NavigationRoutes.StockTake) },
            onOpenSolicitud = { id ->
                navController.navigate(NavigationRoutes.InventoryTransferRequest(id))
            },
            onNuevaSolicitud = {
                navController.navigate(NavigationRoutes.InventoryNewTransferRequest)
            },
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.OrdersList>{
        OrdersListScreen(
            onBack = onBack,
            onOpenOrder = { id -> navController.navigate(NavigationRoutes.Order(id)) },
            onCreateShipping = { navController.navigate(NavigationRoutes.NewShipping) },
        )
    }
    composable<NavigationRoutes.Order>{
        OrderDetailScreen(onBack = onBack)
    }
    composable<NavigationRoutes.NewShipping>{
        CreateShippingScreen(
            onBack = onBack,
            onCreated = { orderId ->
                navController.navigate(NavigationRoutes.Order(orderId)){
                    popUpTo(NavigationRoutes.OrdersList)
                }
            },
        )
    }
    composable<NavigationRoutes.InventoryTransferDetail> {
        TransferDetailScreen(onBack = onBack)
    }
    composable<NavigationRoutes.InventoryNewTransfer> {
        CreateTransferScreen(onCreated = onBack)
    }
    composable<NavigationRoutes.InventoryTransferRequest> {
        TransferRequestDetailScreen(onBack = onBack)
    }
    composable<NavigationRoutes.InventoryNewTransferRequest> {
        CreateTransferRequestScreen(onCreated = onBack)
    }
}