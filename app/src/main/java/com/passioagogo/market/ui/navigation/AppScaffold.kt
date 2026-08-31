package com.passioagogo.market.ui.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MarkEmailUnread
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
import androidx.compose.runtime.LaunchedEffect
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
import com.passioagogo.market.ui.admin.attributes.AttributePresetsScreen
import com.passioagogo.market.ui.admin.catalog.CatalogAdminScreen
import com.passioagogo.market.ui.catalog.browse.ProductDetailScreen
import com.passioagogo.market.ui.admin.catalog.ProductEditScreen
import com.passioagogo.market.ui.admin.contact.ContactMessagesScreen
import com.passioagogo.market.ui.admin.customers.CustomersScreen
import com.passioagogo.market.ui.admin.events.EventEditScreen
import com.passioagogo.market.ui.admin.events.EventsListScreen
import com.passioagogo.market.ui.admin.guides.GuideEditScreen
import com.passioagogo.market.ui.admin.guides.GuidesListScreen
import com.passioagogo.market.ui.admin.gallery.GalleryEditScreen
import com.passioagogo.market.ui.admin.gallery.GalleryListScreen
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
import com.passioagogo.market.ui.inventory.stocktake.StockTakeScreen
import com.passioagogo.market.ui.inventory.transfers.CreateTransferScreen
import com.passioagogo.market.ui.inventory.transfers.TransferDetailScreen
import com.passioagogo.market.ui.orders.OrderDetailScreen
import com.passioagogo.market.ui.orders.OrdersListScreen
import com.passioagogo.market.ui.orders.shipping.CreateShippingScreen
import com.passioagogo.market.ui.pos.PosScreen
import com.passioagogo.market.ui.requests.RequestCartScreen
import com.passioagogo.market.ui.requests.RequestDetailScreen
import com.passioagogo.market.ui.requests.RequestsListScreen
import kotlinx.coroutines.launch

/**
 * Destino pedido por una notificación. Se resuelve una sola vez al abrir:
 * navegar en cada recomposición secuestraría la navegación del usuario.
 */
data class DeepLinkDestino(val tipo: String, val id: String?)

/** Destinos de la barra inferior: solo las tres acciones de operación. */
enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    VENDER("vender", "Vender", Icons.Filled.PointOfSale),
    PEDIDOS("pedidos", "Pedidos", Icons.AutoMirrored.Filled.ReceiptLong),
    INVENTARIO("inventario", "Inventario", Icons.Filled.Inventory2),

    // Del promotor: no vende ni maneja inventario, arma solicitudes.
    CATALOGO_PROMOTOR("admin/catalog", "Catálogo", Icons.Filled.Category),
    CARRITO("promotor/carrito", "Carrito", Icons.Filled.ShoppingCart),
    SOLICITUDES("promotor/solicitudes", "Solicitudes", Icons.AutoMirrored.Filled.ReceiptLong),
}

private val DESTINOS_STAFF = listOf(
    AppDestination.VENDER,
    AppDestination.PEDIDOS,
    AppDestination.INVENTARIO,
)

private val DESTINOS_PROMOTOR = listOf(
    AppDestination.CATALOGO_PROMOTOR,
    AppDestination.CARRITO,
    AppDestination.SOLICITUDES,
)

/** El cliente arma su propio pedido: mismo recorrido, otros rótulos. */
private val DESTINOS_CLIENTE = listOf(
    AppDestination.CATALOGO_PROMOTOR,
    AppDestination.CARRITO,
    AppDestination.SOLICITUDES,
)

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
    ETIQUETAS("admin/presets", "Etiquetas de atributos", Icons.Filled.Label),
    SOLICITUDES_ADMIN("admin/requests", "Solicitudes de promotores", Icons.Filled.AssignmentTurnedIn),
    ESTADISTICAS("admin/stats", "Estadísticas", Icons.Filled.BarChart),
    GALERIA("admin/gallery", "Galería web", Icons.Filled.Collections),
    EVENTOS("admin/events", "Eventos", Icons.Filled.Event),
    GUIAS("admin/guides", "Uso y cuidados", Icons.AutoMirrored.Filled.MenuBook),
    MENSAJES("admin/contact", "Mensajes de contacto", Icons.Filled.MarkEmailUnread),
}

private val ADMIN_SECTIONS = DrawerSection.entries
private val VENDEDOR_SECTIONS = listOf(DrawerSection.CATALOGO, DrawerSection.PROMOCIONES)
private val PROMOTOR_SECTIONS = listOf(DrawerSection.PROMOCIONES)
/** El cliente no gestiona nada: su panel solo ofrece cerrar sesión. */
private val CLIENTE_SECTIONS = emptyList<DrawerSection>()

/** Título de la barra superior según la ruta activa. */
private fun titleFor(route: String?): String = when (route) {
    AppDestination.VENDER.route -> "Vender"
    "pedidos/home" -> "Pedidos"
    "pedidos/order/{orderId}" -> "Detalle del pedido"
    "pedidos/shipping_new" -> "Nuevo envío"
    "inventario/home" -> "Inventario"
    "inventario/transfer/{transferId}" -> "Transferencia"
    "inventario/transfer_new" -> "Nueva transferencia"
    "inventario/stocktake" -> "Registrar existencias"
    "admin/catalog" -> "Catálogo"
    "promotor/carrito" -> "Carrito"
    "promotor/solicitudes" -> "Solicitudes"
    "promotor/solicitud/{requestId}" -> "Detalle de solicitud"
    "admin/requests" -> "Solicitudes de promotores"
    "catalogo/producto/{productId}" -> "Ficha del producto"
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
    "admin/presets" -> "Etiquetas de atributos"
    "admin/stats" -> "Estadísticas"
    "admin/gallery" -> "Galería web"
    "admin/events" -> "Eventos"
    "admin/guides" -> "Uso y cuidados"
    "admin/guide/{guideId}" -> "Editar guía"
    "admin/guide_new" -> "Nueva guía"
    "admin/event/{eventId}" -> "Editar evento"
    "admin/event_new" -> "Nuevo evento"
    "admin/gallery/{itemId}" -> "Editar elemento"
    "admin/gallery_new" -> "Nuevo elemento"
    "admin/contact" -> "Mensajes de contacto"
    else -> "Passion A Gogo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    session: SessionState.Authenticated,
    onSignOut: () -> Unit,
    deepLink: DeepLinkDestino? = null,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Con el panel abierto, «atrás» lo cierra en lugar de navegar por debajo.
    // Solo se activa mientras está abierto: si estuviera siempre activo,
    // bloquearía la navegación normal hacia atrás.
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val currentRoute = currentDestination?.route

    // Las notificaciones son de administración: para otros roles se ignoran.
    LaunchedEffect(deepLink) {
        if (deepLink == null || !session.isAdmin) return@LaunchedEffect
        when (deepLink.tipo) {
            "solicitud" -> deepLink.id?.let {
                navController.navigate("promotor/solicitud/$it")
            }
            "contacto" -> navController.navigate("admin/contact")
        }
    }

    val sections = when {
        session.isAdmin -> ADMIN_SECTIONS
        session.isPromotor -> PROMOTOR_SECTIONS
        session.isCliente -> CLIENTE_SECTIONS
        else -> VENDEDOR_SECTIONS
    }
    val destinos = when {
        session.isCliente -> DESTINOS_CLIENTE
        session.isPromotor -> DESTINOS_PROMOTOR
        else -> DESTINOS_STAFF
    }

    // La barra inferior solo pertenece a las secciones de operación; en las
    // pantallas de gestión se oculta para ganar espacio vertical.
    val enOperacion = destinos.any { dest ->
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
                        destinos.forEach { dest ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == dest.route } == true
                            val rotulo = if (session.isCliente &&
                                dest == AppDestination.SOLICITUDES
                            ) "Mis pedidos" else dest.label
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
                                icon = { Icon(dest.icon, contentDescription = rotulo) },
                                label = { Text(rotulo) },
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
                UserRole.PROMOTOR -> "Promotor"
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
        startDestination = when {
            session.isPromotor || session.isCliente -> AppDestination.CATALOGO_PROMOTOR.route
            else -> AppDestination.VENDER.route
        },
    ) {
        // ---------- Barra inferior ----------
        // El promotor no vende ni toca inventario: esos destinos no existen
        // en su grafo, así que no hay forma de alcanzarlos.
        val soloConsulta = session.isPromotor || session.isCliente
        if (!soloConsulta) {
            composable(AppDestination.VENDER.route) {
                PosScreen()
            }
        }

        // ---------- Carrito de solicitudes: promotor y cliente ----------
        composable(AppDestination.CARRITO.route) {
            RequestCartScreen()
        }
        composable(AppDestination.SOLICITUDES.route) {
            RequestsListScreen(
                onOpenRequest = { id -> navController.navigate("promotor/solicitud/$id") },
            )
        }
        composable(
            route = "promotor/solicitud/{requestId}",
            arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
        ) {
            RequestDetailScreen(onBack = { navController.popBackStack() })
        }

        if (!soloConsulta) navigation(
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

        if (!soloConsulta) navigation(
            route = AppDestination.INVENTARIO.route,
            startDestination = "inventario/home",
        ) {
            composable("inventario/home") {
                InventoryHomeScreen(
                    session = session,
                    onOpenTransfer = { id -> navController.navigate("inventario/transfer/$id") },
                    onCreateTransfer = { navController.navigate("inventario/transfer_new") },
                    onOpenStockTake = { navController.navigate("inventario/stocktake") },
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
            // Ajuste directo de existencias: exclusivo de administración
            if (esAdmin) {
                composable("inventario/stocktake") {
                    StockTakeScreen()
                }
            }
        }

        // ---------- Panel lateral: promociones ----------
        // El vendedor entra en modo consulta; RLS bloquea igualmente cualquier
        // escritura, pero la UI no debe ofrecerla.
        if (!session.isCliente) composable("admin/promotions") {
            PromotionsListScreen(
                readOnly = !esAdmin,
                onOpenPromotion = { id -> navController.navigate("admin/promotion/$id") },
                onNewPromotion = { navController.navigate("admin/promotion_new") },
            )
        }
        if (!session.isCliente) composable(
            route = "admin/promotion/{promotionId}",
            arguments = listOf(navArgument("promotionId") { type = NavType.StringType }),
        ) {
            PromotionEditScreen(
                readOnly = !esAdmin,
                onSaved = { navController.popBackStack() },
            )
        }

        // ---------- Catálogo: todo el staff ----------
        // El vendedor entra siempre en modo consulta; el admin puede alternar.
        composable("admin/catalog") {
            CatalogAdminScreen(
                onOpenProduct = { id -> navController.navigate("admin/product/$id") },
                onNewProduct = { navController.navigate("admin/product_new") },
                onViewProduct = { id -> navController.navigate("catalogo/producto/$id") },
            )
        }
        composable(
            route = "catalogo/producto/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) {
            ProductDetailScreen()
        }

        // ---------- Panel lateral: solo admin ----------
        if (esAdmin) {
            composable("admin/promotion_new") {
                PromotionEditScreen(onSaved = { navController.popBackStack() })
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
            composable("admin/presets") {
                AttributePresetsScreen()
            }
            composable("admin/requests") {
                RequestsListScreen(
                    onOpenRequest = { id -> navController.navigate("promotor/solicitud/$id") },
                )
            }
            composable("admin/stats") {
                StatsScreen()
            }
            composable("admin/gallery") {
                GalleryListScreen(
                    onOpenItem = { id -> navController.navigate("admin/gallery/$id") },
                    onNewItem = { navController.navigate("admin/gallery_new") },
                )
            }
            composable(
                route = "admin/gallery/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
            ) {
                GalleryEditScreen(onSaved = { navController.popBackStack() })
            }
            composable("admin/gallery_new") {
                GalleryEditScreen(onSaved = { navController.popBackStack() })
            }
            composable("admin/contact") {
                ContactMessagesScreen()
            }
            composable("admin/events") {
                EventsListScreen(
                    onOpenEvent = { id -> navController.navigate("admin/event/$id") },
                    onNewEvent = { navController.navigate("admin/event_new") },
                )
            }
            composable(
                route = "admin/event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) {
                EventEditScreen(onSaved = { navController.popBackStack() })
            }
            composable("admin/event_new") {
                EventEditScreen(onSaved = { navController.popBackStack() })
            }
            composable("admin/guides") {
                GuidesListScreen(
                    onOpenGuide = { id -> navController.navigate("admin/guide/$id") },
                    onNewGuide = { navController.navigate("admin/guide_new") },
                )
            }
            composable(
                route = "admin/guide/{guideId}",
                arguments = listOf(navArgument("guideId") { type = NavType.StringType }),
            ) {
                GuideEditScreen(onSaved = { navController.popBackStack() })
            }
            composable("admin/guide_new") {
                GuideEditScreen(onSaved = { navController.popBackStack() })
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
