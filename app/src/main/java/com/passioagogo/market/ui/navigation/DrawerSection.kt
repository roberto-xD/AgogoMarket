package com.passioagogo.market.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

data class DeepLinkDestino(val tipo: String, val id: String?)
enum class DrawerSection(
    val route: NavigationRoutes,
    val label: String,
    val icon: ImageVector,
) {
    CATALOGO         (NavigationRoutes.Catalog(false), "Catálogo"    , Icons.Filled.Category),
    INVENTARIO       (NavigationRoutes.InventoryHome             , "Inventario"  , Icons.Filled.Inventory2),
    VENDER           (NavigationRoutes.PointOfSale               , "Vender"      , Icons.Filled.PointOfSale),
    COMPRAS          (NavigationRoutes.Purchases                 , "Compras"     , Icons.Filled.ShoppingCart),
    PROVEEDORES      (NavigationRoutes.Suppliers                 , "Proveedores" , Icons.Filled.LocalShipping),
    PEDIDOS          (NavigationRoutes.OrdersList                , "Pedidos"     , Icons.AutoMirrored.Filled.ReceiptLong),
    SOLICITUDES_ADMIN(NavigationRoutes.RequestList()             , "Solicitudes de promotores", Icons.Filled.AssignmentTurnedIn),
    ETIQUETAS        (NavigationRoutes.Attributes                , "Etiquetas de atributos", Icons.Filled.Label),
    ESTADISTICAS     (NavigationRoutes.Stats                     , "Estadísticas", Icons.Filled.BarChart),
    UBICACIONES      (NavigationRoutes.Locations                 , "Ubicaciones" , Icons.Filled.Store),
    USUARIOS         (NavigationRoutes.Users                     , "Usuarios"    , Icons.Filled.ManageAccounts),
    PROMOCIONES      (NavigationRoutes.PromotionList             , "Promociones" , Icons.Filled.LocalOffer),
    RULETA           (NavigationRoutes.Wheel                    , "Ruleta de descuentos", Icons.Filled.Casino),
    GALERIA          (NavigationRoutes.GalleryList               , "Galería web" , Icons.Filled.Collections),
    EVENTOS          (NavigationRoutes.EventList                 , "Eventos"     , Icons.Filled.Event),
    GUIAS            (NavigationRoutes.GuidesList                , "Uso y cuidados", Icons.AutoMirrored.Filled.MenuBook),
    MENSAJES         (NavigationRoutes.ContactMessages           , "Mensajes de contacto", Icons.Filled.MarkEmailUnread),
    CLIENTES         (NavigationRoutes.Customers                 , "Clientes"    , Icons.Filled.Groups),
    CARRITO          (NavigationRoutes.ShoppingCart              , "Carrito"     , Icons.Filled.ShoppingCart),
    SOLICITUDES      (NavigationRoutes.RequestList()             , "Solicitudes" , Icons.AutoMirrored.Filled.ReceiptLong),
}
/** Secciones de gestión, accesibles desde el panel lateral. */
val ADMIN_SECTIONS = DrawerSection.entries.filterNot { it == DrawerSection.CARRITO }.filterNot { it == DrawerSection.SOLICITUDES }
val VENDEDOR_SECTIONS = listOf(
    DrawerSection.CATALOGO,
    DrawerSection.PROMOCIONES,
    DrawerSection.VENDER,
    DrawerSection.PEDIDOS,
    DrawerSection.INVENTARIO
)
val PROMOTOR_SECTIONS = listOf(
    DrawerSection.CATALOGO,
    DrawerSection.CARRITO,
    DrawerSection.SOLICITUDES
)
val CLIENTE_SECTIONS = listOf(
    DrawerSection.CATALOGO,
    DrawerSection.CARRITO,
    DrawerSection.SOLICITUDES
)
