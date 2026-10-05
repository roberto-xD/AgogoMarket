package com.passioagogo.market.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationRoutes{
    @Serializable data object MainScreen:NavigationRoutes()
    @Serializable data class Catalog(
        val isConsulta: Boolean
    ):NavigationRoutes()
    @Serializable data class ProductDetail(
        val productId: String
    ):NavigationRoutes()
    @Serializable data class EditProduct(
        val productId: String? = null
    ): NavigationRoutes()
    @Serializable data object StockTake: NavigationRoutes()
    @Serializable data object PromotionList: NavigationRoutes()
    @Serializable data class NewPromotion(
        val promotionId: String? = null
    ): NavigationRoutes()
    @Serializable data object Suppliers: NavigationRoutes()
    @Serializable data object Purchases: NavigationRoutes()
    @Serializable data class PurchaseDetail(
        val purchaseId: String
    ): NavigationRoutes()
    @Serializable data object NewPurchase: NavigationRoutes()
    @Serializable data object Users: NavigationRoutes()
    @Serializable data object Locations: NavigationRoutes()
    @Serializable data object Customers: NavigationRoutes()
    @Serializable data object Attributes: NavigationRoutes()

    @Serializable data class Request(
        // El nombre debe coincidir con la clave que lee RequestDetailViewModel
        val requestId: String
    ): NavigationRoutes()
    @Serializable data object OrdersList: NavigationRoutes()
    @Serializable data class Order(
        val orderId: String
    ): NavigationRoutes()
    @Serializable data object NewShipping: NavigationRoutes()

    @Serializable data object GalleryList: NavigationRoutes()
    @Serializable data class GalleryItem(
        // El nombre debe coincidir con la clave que lee GalleryEditViewModel
        val itemId: String? = null
    ): NavigationRoutes()
    @Serializable data object ContactMessages: NavigationRoutes()
    @Serializable data object EventList: NavigationRoutes()
    @Serializable data class EventEdit(
        val eventId: String? = null
    ): NavigationRoutes()
    @Serializable data object GuidesList: NavigationRoutes()
    @Serializable data class GuidesEdit(
        val guideId: String? = null
    ): NavigationRoutes()
    @Serializable data object Stats: NavigationRoutes()
    @Serializable data object InventoryHome:NavigationRoutes()
    @Serializable data class InventoryTransferDetail(
        val transferId: String
    ): NavigationRoutes()
    @Serializable data object InventoryNewTransfer: NavigationRoutes()
    @Serializable data class InventoryTransferRequest(
        val requestId: String
    ): NavigationRoutes()
    @Serializable data object InventoryNewTransferRequest: NavigationRoutes()
    @Serializable data object PointOfSale:NavigationRoutes()
    @Serializable data object ShoppingCart:NavigationRoutes()
    @Serializable data object RequestCart:NavigationRoutes()
    @Serializable data class RequestList(
        val requestId: String? = null
    ): NavigationRoutes()

}