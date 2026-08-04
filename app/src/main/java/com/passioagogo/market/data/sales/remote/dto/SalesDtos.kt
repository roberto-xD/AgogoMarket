package com.passioagogo.market.data.sales.remote.dto

import com.passioagogo.market.domain.common.OrderStatus
import com.passioagogo.market.domain.common.OrderType
import com.passioagogo.market.domain.common.PaymentMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ 06 · orders ============

@Serializable
data class OrderDto(
    val id: String,
    val folio: Long,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("location_id") val locationId: String,
    val tipo: OrderType = OrderType.MOSTRADOR,
    val estado: OrderStatus = OrderStatus.PENDIENTE,
    @SerialName("costo_envio") val costoEnvio: Double = 0.0,
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val total: Double = 0.0,
    @SerialName("shipping_address_id") val shippingAddressId: String? = null,
    @SerialName("numero_guia") val numeroGuia: String? = null,
    val paqueteria: String? = null,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    /** Poblados cuando el select incluye los recursos embebidos. */
    @SerialName("order_items") val items: List<OrderItemDto> = emptyList(),
    @SerialName("payments") val payments: List<PaymentDto> = emptyList(),
)

@Serializable
data class OrderItemDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("variant_id") val variantId: String,
    val cantidad: Int,
    @SerialName("precio_unitario") val precioUnitario: Double,
    @SerialName("costo_unitario") val costoUnitario: Double = 0.0,
    @SerialName("promotion_id") val promotionId: String? = null,
    val descuento: Double = 0.0,
)

@Serializable
data class PaymentDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val monto: Double,
    val metodo: PaymentMethod,
    val referencia: String? = null,
    val fecha: String? = null,
    @SerialName("received_by") val receivedBy: String,
)

// ---------- Payloads de escritura ----------

@Serializable
data class NewOrderDto(
    @SerialName("location_id") val locationId: String,
    val tipo: OrderType = OrderType.MOSTRADOR,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("shipping_address_id") val shippingAddressId: String? = null,
    @SerialName("costo_envio") val costoEnvio: Double = 0.0,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class NewPaymentDto(
    @SerialName("order_id") val orderId: String,
    val monto: Double,
    val metodo: PaymentMethod,
    val referencia: String? = null,
    @SerialName("received_by") val receivedBy: String,
)

// ============ 12 · RPCs ============

@Serializable
data class PrecioVigenteDto(
    @SerialName("variant_id") val variantId: String,
    @SerialName("precio_lista") val precioLista: Double,
    @SerialName("precio_final") val precioFinal: Double,
    @SerialName("promotion_id") val promotionId: String? = null,
)
