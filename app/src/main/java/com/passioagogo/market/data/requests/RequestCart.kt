package com.passioagogo.market.data.requests

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Artículo del carrito del promotor, con lo necesario para pintarlo. */
data class RequestCartItem(
    val variantId: String,
    val producto: String,
    val sku: String,
    val atributos: String,
    val precio: Double,
    val cantidad: Int,
) {
    val importe: Double get() = precio * cantidad
}

/**
 * Carrito en memoria del promotor. Es Singleton porque se llena desde la
 * ficha de producto y se envía desde otra pantalla: un ViewModel por
 * pantalla perdería el contenido al navegar.
 *
 * No se persiste en disco: una solicitud a medio armar no debería
 * sobrevivir al cierre de la app, y el promotor la rehace en segundos.
 */
@Singleton
class RequestCart @Inject constructor() {

    private val _items = MutableStateFlow<Map<String, RequestCartItem>>(emptyMap())
    val items: StateFlow<Map<String, RequestCartItem>> = _items.asStateFlow()

    fun add(item: RequestCartItem) = _items.update { actual ->
        val existente = actual[item.variantId]
        val nuevo = if (existente == null) item
        else existente.copy(cantidad = existente.cantidad + item.cantidad)
        actual + (item.variantId to nuevo)
    }

    fun setQuantity(variantId: String, cantidad: Int) = _items.update { actual ->
        val existente = actual[variantId] ?: return@update actual
        if (cantidad <= 0) actual - variantId
        else actual + (variantId to existente.copy(cantidad = cantidad))
    }

    fun remove(variantId: String) = _items.update { it - variantId }

    fun clear() = _items.update { emptyMap() }
}
