package com.passioagogo.market.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.passioagogo.market.domain.auth.SessionState
import com.passioagogo.market.domain.common.UserRole
import com.passioagogo.market.ui.admin.catalog.CatalogAdminScreen
import com.passioagogo.market.ui.admin.catalog.ProductEditScreen
import com.passioagogo.market.ui.admin.customers.CustomersScreen
import com.passioagogo.market.ui.admin.locations.LocationsScreen
import com.passioagogo.market.ui.admin.promotions.PromotionEditScreen
import com.passioagogo.market.ui.admin.promotions.PromotionsListScreen
import com.passioagogo.market.ui.admin.purchases.CreatePurchaseScreen
import com.passioagogo.market.ui.admin.purchases.PurchaseDetailScreen
import com.passioagogo.market.ui.admin.purchases.PurchasesListScreen
import com.passioagogo.market.ui.admin.stats.StatsScreen
import com.passioagogo.market.ui.admin.suppliers.SuppliersScreen
import com.passioagogo.market.ui.admin.users.UsersScreen
import com.passioagogo.market.ui.inventory.InventoryHomeScreen
import com.passioagogo.market.ui.inventory.transfers.CreateTransferScreen
import com.passioagogo.market.ui.inventory.transfers.TransferDetailScreen
import com.passioagogo.market.ui.orders.OrderDetailScreen
import com.passioagogo.market.ui.orders.OrdersListScreen
import com.passioagogo.market.ui.orders.shipping.CreateShippingScreen
import com.passioagogo.market.ui.pos.PosScreen
import kotlinx.coroutines.launch

/** Destinos de la barra inferior: solo las tres acciones de operación. */
enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    VENDER("vender", "Vender", Icons.Filled.PointOfSale),
    PEDIDOS("pedidos", "Pedidos", Icons.AutoMirrored.Filled.ReceiptLong),
    INVENTARIO("inventario", "Inventario", Icons.Filled.Inventory2),
}

/** Secciones de gestión, accesibles desde el panel lateral. */
private enum class DrawerSection(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    CATALOGO("admin/catalog", "Catálogo", Icons.Filled.Category),
    PROVEEDORES("admin/suppliers", "Proveedores", Icons.Filled.LocalShipping),
    COMPRAS("admin/purchases", "Compras", Icons.Filled.ShoppingCart),
    PROMOCIONES("admin/promotions", "Promociones", Icons.Filled.LocalOffer),
    USUARIOS("admin/users", "Usuarios", Icons.Filled.ManageAccounts),
    UBICACIONES("admin/locations", "Ubicaciones", Icons.Filled.Store),
    CLIENTES("admin/customers", "Clientes", Icons.Filled.Groups),
    ESTADISTICAS("admin/stats", "Estadísticas", Icons.Filled.BarChart),
}

private val ADMIN_SECTIONS = DrawerSection.entries
private val VENDEDOR_SECTIONS = listOf(DrawerSection.PROMOCIONES)

/** Título de la barra superior según la ruta activa. */
private fun titleFor(route: String?): String = when (route) {
    AppDestination.VENDER.route -> "Vender"
    "pedidos/home" -> "Pedidos"
    "pedidos/order/{orderId}" -> "Detalle del pedido"
    "pedidos/shipping_new" -> "Nuevo envío"
    "inventario/home" -> "Inventario"
    "inventario/transfer/{transferId}" -> "Transferencia"
    "inventario/transfer_new" -> "Nueva transferencia"
    "admin/catalog" -> "Catálogo"
    "admin/product/{productId}" -> "Editar producto"
    "admin/product_new" -> "Nuevo producto"
    "admin/suppliers" -> "Proveedores"
    "admin/purchases" -> "Compras"
    "admin/purchase/{purchaseId}" -> "Detalle de compra"
    "admin/purchase_new" -> "Nueva compra"
    "admin/promotions" -> "Promociones"
    "admin/promotion/{promotionId}" -> "Promoción"
    "admin/promotion_new" -> "Nueva promoción"
    "admin/users" -> "Usuarios"
    "admin/locations" -> "Ubicaciones"
    "admin/customers" -> "Clientes"
    "admin/stats" -> "Estadísticas"
    else -> "Passion A Gogo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    session: SessionState.Authenticated,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val currentRoute = currentDestination?.route

    val sections = if (session.isAdmin) ADMIN_SECTIONS else VENDEDOR_SECTIONS

    // La barra inferior solo pertenece a las secciones de operación; en las
    // pantallas de gestión se oculta para ganar espacio vertical.
    val enOperacion = AppDestination.entries.any { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route } == true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(session)
                HorizontalDivider()
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    sections.forEach { section ->
                        NavigationDrawerItem(
                            label = { Text(section.label) },
                            icon = { Icon(section.icon, contentDescription = null) },
                            selected = currentRoute == section.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigateToSection(section.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSignOut()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titleFor(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                )
            },
            bottomBar = {
                if (enOperacion) {
                    NavigationBar {
                        AppDestination.entries.forEach { dest ->
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
                }
            },
        ) { padding ->
            Column(Modifier.padding(padding)) {
                if (session.vendedorSinTienda) {
                    VendedorSinTiendaBanner()
                }
                AppNavHost(
                    navController = navController,
                    session = session,
                )
            }
        }
    }
}

/** Navegación a una sección del panel: sin apilar duplicados. */
private fun NavHostController.navigateToSection(route: String) {
    navigate(route) { launchSingleTop = true }
}

@Composable
private fun DrawerHeader(session: SessionState.Authenticated) {
    Column(Modifier.padding(24.dp)) {
        Text(
            text = session.profile.nombre,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = when (session.profile.rol) {
                UserRole.ADMIN -> "Administrador"
                UserRole.VENDEDOR -> "Vendedor"
                UserRole.CLIENTE -> "Cliente"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    session: SessionState.Authenticated,
) {
    val esAdmin = session.isAdmin

    NavHost(
        navController = navController,
        startDestination = AppDestination.VENDER.route,
    ) {
        // ---------- Barra inferior ----------
        composable(AppDestination.VENDER.route) {
            PosScreen()
        }

        navigation(
            route = AppDestination.PEDIDOS.route,
            startDestination = "pedidos/home",
        ) {
            composable("pedidos/home") {
                OrdersListScreen(
                    onOpenOrder = { id -> navController.navigate("pedidos/order/$id") },
                    onCreateShipping = { navController.navigate("pedidos/shipping_new") },
                )
            }
            composable(
                route = "pedidos/order/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) {
                OrderDetailScreen(onBack = { navController.popBackStack() })
            }
            composable("pedidos/shipping_new") {
                CreateShippingScreen(
                    onCreated = { orderId ->
                        navController.navigate("pedidos/order/$orderId") {
                            popUpTo("pedidos/home")
                        }
                    },
                )
            }
        }

        navigation(
            route = AppDestination.INVENTARIO.route,
            startDestination = "inventario/home",
        ) {
            composable("inventario/home") {
                InventoryHomeScreen(
                    session = session,
                    onOpenTransfer = { id -> navController.navigate("inventario/transfer/$id") },
                    onCreateTransfer = { navController.navigate("inventario/transfer_new") },
                )
            }
            composable(
                route = "inventario/transfer/{transferId}",
                arguments = listOf(navArgument("transferId") { type = NavType.StringType }),
            ) {
                TransferDetailScreen(onBack = { navController.popBackStack() })
            }
            composable("inventario/transfer_new") {
                CreateTransferScreen(onCreated = { navController.popBackStack() })
            }
        }

        // ---------- Panel lateral: promociones ----------
        // El vendedor entra en modo consulta; RLS bloquea igualmente cualquier
        // escritura, pero la UI no debe ofrecerla.
        composable("admin/promotions") {
            PromotionsListScreen(
                readOnly = !esAdmin,
                onOpenPromotion = { id -> navController.navigate("admin/promotion/$id") },
                onNewPromotion = { navController.navigate("admin/promotion_new") },
            )
        }
        composable(
            route = "admin/promotion/{promotionId}",
            arguments = listOf(navArgument("promotionId") { type = NavType.StringType }),
        ) {
            PromotionEditScreen(
                readOnly = !esAdmin,
                onSaved = { navController.popBackStack() },
            )
        }

        // ---------- Panel lateral: solo admin ----------
        if (esAdmin) {
            composable("admin/promotion_new") {
                PromotionEditScreen(onSaved = { navController.popBackStack() })
            }
            composable("admin/catalog") {
                CatalogAdminScreen(
                    onOpenProduct = { id -> navController.navigate("admin/product/$id") },
                    onNewProduct = { navController.navigate("admin/product_new") },
                )
            }
            composable(
                route = "admin/product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            ) {
                ProductEditScreen()
            }
            composable("admin/product_new") {
                ProductEditScreen()
            }
            composable("admin/suppliers") {
                SuppliersScreen()
            }
            composable("admin/purchases") {
                PurchasesListScreen(
                    onOpenPurchase = { id -> navController.navigate("admin/purchase/$id") },
                    onCreatePurchase = { navController.navigate("admin/purchase_new") },
                )
            }
            composable(
                route = "admin/purchase/{purchaseId}",
                arguments = listOf(navArgument("purchaseId") { type = NavType.StringType }),
            ) {
                PurchaseDetailScreen(onBack = { navController.popBackStack() })
            }
            composable("admin/purchase_new") {
                CreatePurchaseScreen(onCreated = { navController.popBackStack() })
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
            composable("admin/stats") {
                StatsScreen()
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
