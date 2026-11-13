package com.passioagogo.market.data.pagos

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Lo cobrado por una venta del sitio y lo que de verdad llegó.
 *
 * `netoTrasGuia` **no es la ganancia**: descuenta la comisión de la
 * pasarela y lo que costó la guía, nada más. El costo de los productos
 * vive en el catálogo y no entra aquí.
 */
data class Cobro(
    val id: String,
    val folio: Long,
    val codigoPedido: String,
    val clienteNombre: String,
    val estado: String,
    val createdAt: String?,
    // ---- Lo que se le enseñó ----
    val totalEstimado: Double,
    val envioCosto: Double?,
    val envioGratis: Boolean,
    val codigoDescuento: String?,
    val descuentoPct: Int?,
    val descuentoMonto: Double?,
    val regalo: String?,
    // ---- Lo que se cobró ----
    val pagoEstado: String?,
    val pagoTotal: Double?,
    val pagoMetodo: String?,
    val pagoMarca: String?,
    val pagoUltimos4: String?,
    val pagoReferencia: String?,
    val pagoActualizadoEn: String?,
    // ---- Lo que llegó ----
    val pagoNeto: Double?,
    val pagoComision: Double?,
    val pagoLiberacion: String?,
    val guiaCosto: Double?,
    /** Neto menos guía. Null mientras no se sepa el neto, no cero. */
    val netoTrasGuia: Double?,
) {
    val aprobado: Boolean get() = pagoEstado == "aprobado"

    /** La pasarela aún no ha soltado el dinero. */
    fun pendienteDeLiberar(ahoraIso: String): Boolean =
        aprobado && pagoLiberacion != null && pagoLiberacion > ahoraIso
}

@Serializable
data class CobroDto(
    val id: String,
    val folio: Long = 0,
    @SerialName("codigo_pedido") val codigoPedido: String = "",
    @SerialName("cliente_nombre") val clienteNombre: String = "",
    val estado: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("total_estimado") val totalEstimado: Double = 0.0,
    @SerialName("envio_costo") val envioCosto: Double? = null,
    @SerialName("envio_gratis") val envioGratis: Boolean = false,
    @SerialName("codigo_descuento") val codigoDescuento: String? = null,
    @SerialName("descuento_pct") val descuentoPct: Int? = null,
    @SerialName("descuento_monto") val descuentoMonto: Double? = null,
    val regalo: String? = null,
    @SerialName("pago_estado") val pagoEstado: String? = null,
    @SerialName("pago_total") val pagoTotal: Double? = null,
    @SerialName("pago_metodo") val pagoMetodo: String? = null,
    @SerialName("pago_marca") val pagoMarca: String? = null,
    @SerialName("pago_ultimos4") val pagoUltimos4: String? = null,
    @SerialName("pago_referencia") val pagoReferencia: String? = null,
    @SerialName("pago_actualizado_en") val pagoActualizadoEn: String? = null,
    @SerialName("pago_neto") val pagoNeto: Double? = null,
    @SerialName("pago_comision") val pagoComision: Double? = null,
    @SerialName("pago_liberacion") val pagoLiberacion: String? = null,
    @SerialName("guia_costo") val guiaCosto: Double? = null,
    @SerialName("neto_tras_guia") val netoTrasGuia: Double? = null,
) {
    fun toDomain() = Cobro(
        id = id, folio = folio, codigoPedido = codigoPedido,
        clienteNombre = clienteNombre,
        estado = estado.orEmpty(), createdAt = createdAt,
        totalEstimado = totalEstimado, envioCosto = envioCosto,
        envioGratis = envioGratis, codigoDescuento = codigoDescuento,
        descuentoPct = descuentoPct, descuentoMonto = descuentoMonto, regalo = regalo,
        pagoEstado = pagoEstado, pagoTotal = pagoTotal, pagoMetodo = pagoMetodo,
        pagoMarca = pagoMarca, pagoUltimos4 = pagoUltimos4,
        pagoReferencia = pagoReferencia, pagoActualizadoEn = pagoActualizadoEn,
        pagoNeto = pagoNeto, pagoComision = pagoComision,
        pagoLiberacion = pagoLiberacion, guiaCosto = guiaCosto,
        netoTrasGuia = netoTrasGuia,
    )
}

/**
 * Lee por `fn_pagos_admin`, no por la tabla.
 *
 * El margen de la tienda —comisión, neto y costo de la guía— está
 * revocado para el rol `authenticated`, que es el mismo con el que entra
 * el admin. La función corre con los permisos de su dueño y comprueba el
 * rol ella misma.
 */
interface PagosRepository {
    suspend fun getCobros(
        desde: String? = null,
        hasta: String? = null,
        soloAprobadas: Boolean = false,
        limite: Int = 200,
        /** Busca por el código que dicta el cliente. */
        codigo: String? = null,
    ): DataResult<List<Cobro>>
}

@Singleton
class PagosRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PagosRepository {

    private companion object { const val RPC = "fn_pagos_admin" }

    override suspend fun getCobros(
        desde: String?,
        hasta: String?,
        soloAprobadas: Boolean,
        limite: Int,
        codigo: String?,
    ): DataResult<List<Cobro>> = withContext(io) {
        safeSupabaseCall("getCobros") {
            postgrest.rpc(
                RPC,
                buildJsonObject {
                    // El rango va por la fecha del cobro, no la de la
                    // solicitud: el dinero de noviembre es el que entró
                    // en noviembre.
                    put("p_desde", desde?.let { JsonPrimitive(it) } ?: JsonNull)
                    put("p_hasta", hasta?.let { JsonPrimitive(it) } ?: JsonNull)
                    put("p_solo_aprobadas", soloAprobadas)
                    put("p_limite", limite)
                    put("p_codigo", codigo?.let { JsonPrimitive(it) } ?: JsonNull)
                },
            ).decodeList<CobroDto>()
        }.map { list -> list.map { it.toDomain() } }
    }
}
