package com.passioagogo.market.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.passioagogo.market.domain.auth.SessionState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.navigation
import com.passioagogo.market.ui.admin.catalog.AdminHomeScreen
import com.passioagogo.market.ui.admin.catalog.CatalogAdminScreen
import com.passioagogo.market.ui.admin.catalog.ProductEditScreen
import com.passioagogo.market.ui.admin.purchases.CreatePurchaseScreen
import com.passioagogo.market.ui.admin.purchases.PurchaseDetailScreen
import com.passioagogo.market.ui.admin.purchases.PurchasesListScreen
import com.passioagogo.market.ui.admin.customers.CustomersScreen
import com.passioagogo.market.ui.admin.locations.LocationsScreen
import com.passioagogo.market.ui.orders.shipping.CreateShippingScreen
import com.passioagogo.market.ui.admin.promotions.PromotionEditScreen
import com.passioagogo.market.ui.admin.promotions.PromotionsListScreen
import com.passioagogo.market.ui.admin.suppliers.SuppliersScreen
import com.passioagogo.market.ui.admin.users.UsersScreen
import com.passioagogo.market.ui.inventory.InventoryHomeScreen
import com.passioagogo.market.ui.orders.OrderDetailScreen
import com.passioagogo.market.ui.orders.OrdersListScreen
import com.passioagogo.market.ui.pos.PosScreen
import com.passioagogo.market.ui.inventory.transfers.CreateTransferScreen
import com.passioagogo.market.ui.inventory.transfers.TransferDetailScreen

enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    VENDER("vender", "Vender", Icons.Filled.PointOfSale),
    PEDIDOS("pedidos", "Pedidos", Icons.AutoMirrored.Filled.ReceiptLong),
    INVENTARIO("inventario", "Inventario", Icons.Filled.Inventory2),
    ADMIN("admin", "Administración", Icons.Filled.AdminPanelSettings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    session: SessionState.Authenticated,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination

    val destinations = buildList {
        add(AppDestination.VENDER)
        add(AppDestination.PEDIDOS)
        add(AppDestination.INVENTARIO)
        if (session.isAdmin) add(AppDestination.ADMIN)
    }
    val currentLabel = destinations
        .firstOrNull { dest ->
            currentDestination?.hierarchy?.any { it.route == dest.route } == true
        }?.label ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentLabel) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { dest ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (session.vendedorSinTienda) {
                VendedorSinTiendaBanner()
            }
            NavHost(
                navController = navController,
                startDestination = AppDestination.VENDER.route,
            ) {
                composable(AppDestination.VENDER.route) {
                    PosScreen()
                }
                navigation(
                    route = AppDestination.PEDIDOS.route,
                    startDestination = "pedidos/home",
                ) {
                    composable("pedidos/home") {
                        OrdersListScreen(
                            onOpenOrder = { id ->
                                navController.navigate("pedidos/order/" + id)
                            },
                            onCreateShipping = {
                                navController.navigate("pedidos/shipping_new")
                            },
                        )
                    }
                    composable("pedidos/shipping_new") {
                        CreateShippingScreen(
                            onCreated = { orderId ->
                                navController.navigate("pedidos/order/" + orderId) {
                                    popUpTo("pedidos/home")
                                }
                            },
                        )
                    }
                    composable(
                        route = "pedidos/order/{orderId}",
                        arguments = listOf(
                            navArgument("orderId") { type = NavType.StringType }
                        ),
                    ) {
                        OrderDetailScreen(onBack = { navController.popBackStack() })
                    }
                }
                navigation(
                    route = AppDestination.INVENTARIO.route,
                    startDestination = "inventario/home",
                ) {
                    composable("inventario/home") {
                        InventoryHomeScreen(
                            session = session,
                            onOpenTransfer = { id ->
                                navController.navigate("inventario/transfer/" + id)
                            },
                            onCreateTransfer = {
                                navController.navigate("inventario/transfer_new")
                            },
                        )
                    }
                    composable(
                        route = "inventario/transfer/{transferId}",
                        arguments = listOf(
                            navArgument("transferId") { type = NavType.StringType }
                        ),
                    ) {
                        TransferDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable("inventario/transfer_new") {
                        CreateTransferScreen(
                            onCreated = { navController.popBackStack() },
                        )
                    }
                }
                navigation(
                    route = AppDestination.ADMIN.route,
                    startDestination = "admin/home",
                ) {
                    composable("admin/home") {
                        AdminHomeScreen(
                            onOpenCatalog = { navController.navigate("admin/catalog") },
                            onOpenSuppliers = { navController.navigate("admin/suppliers") },
                            onOpenPurchases = { navController.navigate("admin/purchases") },
                            onOpenPromotions = { navController.navigate("admin/promotions") },
                            onOpenUsers = { navController.navigate("admin/users") },
                            onOpenLocations = { navController.navigate("admin/locations") },
                            onOpenCustomers = { navController.navigate("admin/customers") },
                        )
                    }
                    composable("admin/suppliers") {
                        SuppliersScreen()
                    }
                    composable("admin/purchases") {
                        PurchasesListScreen(
                            onOpenPurchase = { id ->
                                navController.navigate("admin/purchase/" + id)
                            },
                            onCreatePurchase = {
                                navController.navigate("admin/purchase_new")
                            },
                        )
                    }
                    composable(
                        route = "admin/purchase/{purchaseId}",
                        arguments = listOf(
                            navArgument("purchaseId") { type = NavType.StringType }
                        ),
                    ) {
                        PurchaseDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable("admin/promotions") {
                        PromotionsListScreen(
                            onOpenPromotion = { id ->
                                navController.navigate("admin/promotion/" + id)
                            },
                            onNewPromotion = {
                                navController.navigate("admin/promotion_new")
                            },
                        )
                    }
                    composable(
                        route = "admin/promotion/{promotionId}",
                        arguments = listOf(
                            navArgument("promotionId") { type = NavType.StringType }
                        ),
                    ) {
                        PromotionEditScreen(onSaved = { navController.popBackStack() })
                    }
                    composable("admin/promotion_new") {
                        PromotionEditScreen(onSaved = { navController.popBackStack() })
                    }
                    composable("admin/users") {
                        UsersScreen()
                    }
                    composable("admin/locations") {
                        LocationsScreen()
                    }
                    composable("admin/customers") {
                        CustomersScreen()
                    }
                    composable("admin/purchase_new") {
                        CreatePurchaseScreen(
                            onCreated = { navController.popBackStack() },
                        )
                    }
                    composable("admin/catalog") {
                        CatalogAdminScreen(
                            onOpenProduct = { id ->
                                navController.navigate("admin/product/" + id)
                            },
                            onNewProduct = { navController.navigate("admin/product_new") },
                        )
                    }
                    composable(
                        route = "admin/product/{productId}",
                        arguments = listOf(
                            navArgument("productId") { type = NavType.StringType }
                        ),
                    ) {
                        ProductEditScreen()
                    }
                    composable("admin/product_new") {
                        ProductEditScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun VendedorSinTiendaBanner() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "No tienes tienda asignada: pide a un administrador que " +
                "te asigne una para poder operar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}
