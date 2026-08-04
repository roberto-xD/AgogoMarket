package com.passioagogo.market.domain.sales

import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.OrderStatus
import com.passioagogo.market.domain.common.OrderType
import com.passioagogo.market.domain.common.PaymentMethod

// ============ Modelos ============

data class Order(
    val id: String,
    val folio: Long,
    val customerId: String?,
    val locationId: String,
    val tipo: OrderType,
    val estado: OrderStatus,
    val costoEnvio: Double,
    val subtotal: Double,
    val descuento: Double,
    val total: Double,
    val shippingAddressId: String?,
    val numeroGuia: String?,
    val paqueteria: String?,
    val notas: String?,
    val createdBy: String,
    val confirmedAt: String?,
    val createdAt: String?,
    val items: List<OrderItem>,
    val payments: List<Payment>,
)

data class OrderItem(
    val id: String,
    val variantId: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val promotionId: String?,
    val descuento: Double,
)

data class Payment(
    val id: String,
    val monto: Double,
    val metodo: PaymentMethod,
    val referencia: String?,
    val fecha: String?,
)

/** Precio calculado por el servidor (fn_precio_vigente). */
data class PrecioVigente(
    val variantId: String,
    val precioLista: Double,
    val precioFinal: Double,
    val promotionId: String?,
) {
    val tienePromo: Boolean get() = promotionId != null
}

/** Línea del carrito local del POS (opción A: vive en el ViewModel). */
data class CartLine(
    val variantId: String,
    val cantidad: Int,
)

data class CheckoutRequest(
    val locationId: String,
    val lines: List<CartLine>,
    val metodo: PaymentMethod,
    val referencia: String? = null,
    val customerId: String? = null,
    /** Descuento manual a nivel pedido (0 = sin descuento). */
    val descuentoPedido: Double = 0.0,
    val notas: String? = null,
)

// ============ Repositorio ============

data class ShippingOrderDraft(
    val customerId: String,
    val shippingAddressId: String,
    /** Ubicación desde donde se despacha (de ahí sale el stock). */
    val locationId: String,
    val costoEnvio: Double,
    val lines: List<CartLine>,
    val notas: String? = null,
)

/**
 * Ventas de mostrador (Fase 3a) y envíos (Fase 3b). Remote-first estricto: sin red no hay
 * venta (la cola offline quedó como pendiente post-MVP).
 * Los precios SIEMPRE los calcula el servidor (script 12).
 */
interface SalesRepository {

    /** Precio con promo aplicada, para mostrar en catálogo/carrito. */
    suspend fun getPrecioVigente(variantId: String): DataResult<PrecioVigente>

    /**
     * Venta de mostrador completa: crea el pedido, agrega líneas vía
     * fn_agregar_item (precio congelado en servidor), aplica descuento
     * de pedido si lo hay, confirma (descuenta stock) y registra el
     * pago por el total.
     *
     * Si falla al agregar líneas o al confirmar (p. ej. stock
     * insuficiente), el pedido se cancela y se devuelve el error
     * original. Si el pago falla tras confirmar, se devuelve
     * DataError.Business con el folio: el pedido queda visible en
     * v_saldos_pendientes para cobrarse manualmente.
     */
    suspend fun checkout(request: CheckoutRequest): DataResult<Order>

    /** Pedido con líneas y pagos embebidos. */
    suspend fun getOrder(id: String): DataResult<Order>

    /** Últimos pedidos de una ubicación (historial del POS). */
    suspend fun getRecentOrders(locationId: String? = null, limit: Int = 30): DataResult<List<Order>>

    /** Cancela un pedido; si estaba confirmado el trigger devuelve el stock. */
    suspend fun cancelOrder(id: String): DataResult<Order>

    // -- Envíos (Fase 3b) --

    /**
     * Crea y CONFIRMA un pedido de envío (congela snapshot de dirección
     * y descuenta stock). El pago se registra por separado: el pedido
     * queda en v_saldos_pendientes hasta cubrirse.
     */
    suspend fun createShippingOrder(draft: ShippingOrderDraft): DataResult<Order>

    /** confirmado → en_transito, registrando paquetería y guía. */
    suspend fun shipOrder(id: String, paqueteria: String, numeroGuia: String): DataResult<Order>

    /** en_transito → entregado. */
    suspend fun deliverOrder(id: String): DataResult<Order>

    /** Registra un pago (parcial o total) sobre un pedido. */
    suspend fun addPayment(
        orderId: String,
        monto: Double,
        metodo: com.passioagogo.market.domain.common.PaymentMethod,
        referencia: String? = null,
    ): DataResult<Order>
}
