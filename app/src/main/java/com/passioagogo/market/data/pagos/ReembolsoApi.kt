package com.passioagogo.market.data.pagos

import android.util.Log
import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "PG"

/** Una cancelación pedida por el cliente, con el dinero aún cobrado. */
data class PorReembolsar(
    val id: String,
    val folio: Long,
    val clienteNombre: String,
    val clienteTelefono: String?,
    val pagoTotal: Double?,
    val pagoReferencia: String?,
    val pagoEstado: String?,
    val guiaNumero: String?,
    val codigoDescuento: String?,
    val solicitadaAt: String?,
    val motivo: String?,
    val reembolsoAt: String?,
    val reembolsoMonto: Double?,
) {
    /**
     * Con guía no se devuelve: el paquete puede ir en camino. Primero se
     * cancela la guía, y entonces el reembolso deja pasar.
     */
    val bloqueadaPorGuia: Boolean get() = !guiaNumero.isNullOrBlank()

    val yaReembolsada: Boolean get() = reembolsoAt != null
}

@Serializable
data class PorReembolsarDto(
    val id: String,
    val folio: Long,
    @SerialName("cliente_nombre") val clienteNombre: String = "",
    @SerialName("cliente_telefono") val clienteTelefono: String? = null,
    @SerialName("pago_total") val pagoTotal: Double? = null,
    @SerialName("pago_referencia") val pagoReferencia: String? = null,
    @SerialName("pago_estado") val pagoEstado: String? = null,
    @SerialName("guia_numero") val guiaNumero: String? = null,
    @SerialName("codigo_descuento") val codigoDescuento: String? = null,
    @SerialName("cancelacion_solicitada_at") val solicitadaAt: String? = null,
    @SerialName("cancelacion_motivo") val motivo: String? = null,
    @SerialName("reembolso_at") val reembolsoAt: String? = null,
    @SerialName("reembolso_monto") val reembolsoMonto: Double? = null,
) {
    fun toDomain() = PorReembolsar(
        id = id, folio = folio, clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono, pagoTotal = pagoTotal,
        pagoReferencia = pagoReferencia, pagoEstado = pagoEstado,
        guiaNumero = guiaNumero, codigoDescuento = codigoDescuento,
        solicitadaAt = solicitadaAt, motivo = motivo,
        reembolsoAt = reembolsoAt, reembolsoMonto = reembolsoMonto,
    )
}

/** Lo que devolvió la pasarela. */
@Serializable
data class Reembolso(
    val ok: Boolean = false,
    val folio: Long = 0,
    @SerialName("reembolsoId") val reembolsoId: String = "",
    val monto: Double = 0.0,
    /** false = parcial: la compra sigue en pie y no queda cancelada. */
    val total: Boolean = false,
    val estado: String = "",
    @SerialName("codigoDevuelto") val codigoDevuelto: String? = null,
    /** false = el dinero salió pero no se pudo anotar en la solicitud. */
    val guardado: Boolean = true,
)

sealed interface ResultadoReembolso {
    data class Ok(val reembolso: Reembolso) : ResultadoReembolso
    data class Fallo(val mensaje: String) : ResultadoReembolso
}

/**
 * Cancelaciones pendientes de devolver, y la devolución en sí.
 *
 * Devolver el dinero pasa por una Edge Function porque el token de la
 * pasarela no puede vivir en el APK. Desde aquí solo se lee la cola y se
 * pide la devolución.
 */
@Singleton
class ReembolsoApi @Inject constructor(
    private val supabase: SupabaseClient,
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private companion object {
        const val TABLE = "order_requests"
        // Solo columnas concedidas: el 47 revocó la tabla entera y un
        // `*` haría fallar la consulta completa.
        val COLUMNAS = Columns.raw(
            "id, folio, cliente_nombre, cliente_telefono, pago_total, " +
                "pago_referencia, pago_estado, guia_numero, codigo_descuento, " +
                "cancelacion_solicitada_at, cancelacion_motivo, " +
                "reembolso_at, reembolso_monto",
        )
    }

    /**
     * La cola de devoluciones: cancelación pedida y dinero aún cobrado.
     *
     * Con [incluirHechas] se ven también las ya devueltas, para consultar
     * un caso concreto sin salir de la pantalla.
     */
    suspend fun porReembolsar(
        incluirHechas: Boolean = false,
    ): DataResult<List<PorReembolsar>> = withContext(io) {
        safeSupabaseCall("porReembolsar") {
            postgrest.from(TABLE).select(COLUMNAS) {
                // Las pedidas primero; las que no tienen fecha caen al
                // final y se descartan abajo. Se filtra en Kotlin para no
                // depender de cómo exprese PostgREST un «is not null».
                order(
                    "cancelacion_solicitada_at",
                    Order.ASCENDING,
                    nullsFirst = false,
                )
                limit(100)
            }.decodeList<PorReembolsarDto>()
        }.map { lista ->
            lista.map { it.toDomain() }
                .filter { it.solicitadaAt != null }
                .filter { incluirHechas || !it.yaReembolsada }
        }
    }

    /**
     * Pide la devolución.
     *
     * [monto] null devuelve todo; un importe menor hace una parcial, que
     * NO cancela la solicitud: una compra devuelta a medias sigue en pie.
     *
     * [devolverCodigo] suelta el código para que vuelva a usarse. Por
     * omisión sí: a quien se le devuelve el dinero se le devuelve el
     * premio. Se desactiva cuando se devuelve por abuso.
     */
    suspend fun reembolsar(
        solicitudId: String,
        monto: Double? = null,
        nota: String? = null,
        devolverCodigo: Boolean = true,
    ): ResultadoReembolso = withContext(io) {
        if (supabase.auth.currentSessionOrNull() == null) {
            return@withContext ResultadoReembolso.Fallo(
                "No hay sesión abierta. Vuelve a entrar."
            )
        }
        try {
            val cuerpo = buildString {
                append("{\"solicitudId\":\"").append(solicitudId).append("\"")
                monto?.let { append(",\"monto\":").append(it) }
                nota?.takeIf { it.isNotBlank() }?.let {
                    append(",\"nota\":\"").append(it.replace("\"", "'")).append("\"")
                }
                if (!devolverCodigo) append(",\"devolverCodigo\":false")
                append("}")
            }

            val respuesta = supabase.functions("reembolsar") {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(cuerpo)
            }
            val texto = respuesta.bodyAsText()

            if (respuesta.status.value in 200..299) {
                ResultadoReembolso.Ok(json.decodeFromString<Reembolso>(texto))
            } else {
                Log.i(TAG, "[HTTP ${respuesta.status.value}] reembolsar :: $texto")
                ResultadoReembolso.Fallo(mensajeDeError(texto, respuesta.status.value))
            }
        } catch (e: Exception) {
            Log.i(TAG, "[reembolsar] sin respuesta :: ${e.message}")
            ResultadoReembolso.Fallo(
                "No se pudo conectar: ${e.message ?: "error desconocido"}"
            )
        }
    }

    private fun mensajeDeError(cuerpo: String, codigo: Int): String {
        val fallo = runCatching {
            json.decodeFromString<ErrorReembolso>(cuerpo)
        }.getOrNull()
        return fallo?.error ?: when (codigo) {
            401 -> "Tu sesión caducó. Vuelve a entrar."
            403 -> "Tu cuenta no tiene permiso para reembolsar."
            else -> "No se pudo completar la devolución ($codigo)."
        }
    }
}

@Serializable
private data class ErrorReembolso(val error: String? = null)
