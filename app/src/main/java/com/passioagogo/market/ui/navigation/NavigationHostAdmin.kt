package com.passioagogo.market.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.passioagogo.market.ui.admin.attributes.AttributePresetsScreen
import com.passioagogo.market.ui.admin.catalog.ProductEditScreen
import com.passioagogo.market.ui.admin.contact.ContactMessagesScreen
import com.passioagogo.market.ui.admin.customers.CustomersScreen
import com.passioagogo.market.ui.admin.events.EventEditScreen
import com.passioagogo.market.ui.admin.events.EventsListScreen
import com.passioagogo.market.ui.admin.gallery.GalleryEditScreen
import com.passioagogo.market.ui.admin.gallery.GalleryListScreen
import com.passioagogo.market.ui.admin.guides.GuideEditScreen
import com.passioagogo.market.ui.admin.guides.GuidesListScreen
import com.passioagogo.market.ui.admin.locations.LocationsScreen
import com.passioagogo.market.ui.admin.purchases.CreatePurchaseScreen
import com.passioagogo.market.ui.admin.purchases.PurchaseDetailScreen
import com.passioagogo.market.ui.admin.purchases.PurchasesListScreen
import com.passioagogo.market.ui.admin.stats.StatsScreen
import com.passioagogo.market.ui.admin.suppliers.SuppliersScreen
import com.passioagogo.market.ui.admin.users.UsersScreen
import com.passioagogo.market.ui.inventory.stocktake.StockTakeScreen
import com.passioagogo.market.ui.requests.RequestDetailScreen

internal fun NavGraphBuilder.NavigationHostAdmin(
    navController: NavHostController,
    onBack: ()-> Unit,
){
    composable<NavigationRoutes.EditProduct>{
        ProductEditScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.StockTake> {
        StockTakeScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.Suppliers>{
        SuppliersScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.Purchases>{
        PurchasesListScreen(
            onBack = onBack,
            onOpenPurchase = {
                navController.navigate(NavigationRoutes.PurchaseDetail(it))
            },
            onCreatePurchase = {
                navController.navigate(NavigationRoutes.NewPurchase)
            },
        )
    }
    composable<NavigationRoutes.PurchaseDetail>{
        PurchaseDetailScreen(onBack = onBack)
    }
    composable<NavigationRoutes.NewPurchase> {
        CreatePurchaseScreen(onCreated = onBack)
    }
    composable<NavigationRoutes.Users>{
        UsersScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.Locations>{
        LocationsScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.Customers>{
        CustomersScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.Attributes>{
        AttributePresetsScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.Request>{
        RequestDetailScreen(onBack = onBack)
    }
    composable<NavigationRoutes.GalleryList>{
        GalleryListScreen(
            onBack = onBack,
            onOpenItem = { id -> navController.navigate(NavigationRoutes.GalleryItem(id)) },
            onNewItem = { navController.navigate(NavigationRoutes.GalleryItem()) },
        )
    }
    composable<NavigationRoutes.GalleryItem>{
        GalleryEditScreen(
            onBack = onBack,
            onSaved = onBack
        )
    }
    composable<NavigationRoutes.ContactMessages>{
        ContactMessagesScreen(
            onBack = onBack,
        )
    }
    composable<NavigationRoutes.EventList>{
        EventsListScreen(
            onBack = onBack,
            onOpenEvent = { id -> navController.navigate(NavigationRoutes.EventEdit(id)) },
            onNewEvent = { navController.navigate(NavigationRoutes.EventEdit()) },
        )
    }
    composable<NavigationRoutes.EventEdit>{
        EventEditScreen(onSaved = onBack)
    }
    composable<NavigationRoutes.GuidesList>{
        GuidesListScreen(
            onBack = onBack,
            onOpenGuide = { id -> navController.navigate(NavigationRoutes.GuidesEdit(id)) },
            onNewGuide = { navController.navigate(NavigationRoutes.GuidesEdit()) },
        )
    }
    composable<NavigationRoutes.GuidesEdit>{
        GuideEditScreen(onSaved = onBack)
    }
    composable<NavigationRoutes.Stats>{
        StatsScreen(
            onBack = onBack,
        )
    }
    NavigationHostStaff(
        navController = navController,
        onBack = onBack
    )
}