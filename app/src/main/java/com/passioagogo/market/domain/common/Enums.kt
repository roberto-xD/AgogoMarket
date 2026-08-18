package com.passioagogo.market.domain.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Espejo de los ENUMs de 00_enums_setup.sql.
 * Los @SerialName coinciden con los valores de PostgreSQL.
 */

@Serializable
enum class LocationType {
    @SerialName("tienda") TIENDA,
    @SerialName("bodega") BODEGA,
    @SerialName("online") ONLINE,
}

@Serializable
enum class TransferStatus {
    @SerialName("pendiente") PENDIENTE,
    @SerialName("en_transito") EN_TRANSITO,
    @SerialName("recibida") RECIBIDA,
    @SerialName("cancelada") CANCELADA,
}

@Serializable
enum class UserRole {
    @SerialName("admin") ADMIN,
    @SerialName("vendedor") VENDEDOR,
    @SerialName("cliente") CLIENTE,
    /** Muestra catálogo y levanta solicitudes; sin tienda ni inventario. */
    @SerialName("promotor") PROMOTOR,
}

@Serializable
enum class RequestStatus {
    @SerialName("enviada") ENVIADA,
    @SerialName("atendida") ATENDIDA,
    @SerialName("rechazada") RECHAZADA,
}

@Serializable
enum class OrderType {
    @SerialName("mostrador") MOSTRADOR,
    @SerialName("envio") ENVIO,
}

@Serializable
enum class OrderStatus {
    @SerialName("pendiente") PENDIENTE,
    @SerialName("confirmado") CONFIRMADO,
    @SerialName("en_transito") EN_TRANSITO,
    @SerialName("entregado") ENTREGADO,
    @SerialName("cancelado") CANCELADO,
}

@Serializable
enum class PurchaseStatus {
    @SerialName("pendiente") PENDIENTE,
    @SerialName("recibida") RECIBIDA,
    @SerialName("cancelada") CANCELADA,
}

@Serializable
enum class PromotionType {
    @SerialName("porcentaje") PORCENTAJE,
    @SerialName("monto_fijo") MONTO_FIJO,
    @SerialName("precio_especial") PRECIO_ESPECIAL,
}

@Serializable
enum class PaymentMethod {
    @SerialName("efectivo") EFECTIVO,
    @SerialName("transferencia") TRANSFERENCIA,
    @SerialName("tarjeta") TARJETA,
}
