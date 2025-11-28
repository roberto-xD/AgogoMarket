package com.passioagogo.market.presentation.navigation

sealed class Routes(val Name: String) {
    object Dashboard: Routes("Dashboard")
    object Login: Routes("Login")
    object Singup: Routes("Singup")
    object Search: Routes("Search")
    object Import: Routes("Import")
    object ImageGallery: Routes("ImageGallery")
    object ItemDetail: Routes("ItemDetail")
    object SellProcess: Routes("SellProcess")

}