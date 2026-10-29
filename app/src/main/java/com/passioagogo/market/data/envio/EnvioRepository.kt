package com.passioagogo.market.data.envio

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Una solicitud desde la óptica del envío.
 *
 * No trae la dirección: esa vive cifrada y solo se abre en el momento
 * de generar la guía, con la clave privada del administrador.
 */
data class EnvioSolicitud(
    val id: String,
    val folio: Long,
    val clienteNombre: String,
    val clienteTelefono: String?,
    val estado: String,
    val userId: String?,
    /** Paquetería y servicio que eligió el cliente al cotizar. */
    val envioPaqueteria: String?,
    val envioServicio: String?,
    val guiaNumero: String?,
    val guiaUrl: String?,
    val guiaRastreoUrl: String?,
    val guiaPaqueteria: String?,
    val guiaServicio: String?,
    val guiaCosto: Double?,
    val guiaGeneradaEn: String?,
    val necesitaRecoleccion: Boolean,
    val recoleccionId: String?,
    val recoleccionFecha: String?,
    val createdAt: String?,
) {
    val tieneGuia: Boolean get() = !guiaNumero.isNullOrBlank()

    /** Marcada para recoger y aún sin pedir el camión. */
    val esperandoRecoleccion: Boolean
        get() = tieneGuia && necesitaRecoleccion && recoleccionId.isNullOrBlank()
}

@Serializable
data class EnvioSolicitudDto(
    val id: String,
    val folio: Long,
    @SerialName("cliente_nombre") val clienteNombre: String,
    @SerialName("cliente_telefono") val clienteTelefono: String? = null,
    val estado: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("envio_paqueteria") val envioPaqueteria: String? = null,
    @SerialName("envio_servicio") val envioServicio: String? = null,
    @SerialName("guia_numero") val guiaNumero: String? = null,
    @SerialName("guia_url") val guiaUrl: String? = null,
    @SerialName("guia_rastreo_url") val guiaRastreoUrl: String? = null,
    @SerialName("guia_paqueteria") val guiaPaqueteria: String? = null,
    @SerialName("guia_servicio") val guiaServicio: String? = null,
    @SerialName("guia_costo") val guiaCosto: Double? = null,
    @SerialName("guia_generada_en") val guiaGeneradaEn: String? = null,
    @SerialName("necesita_recoleccion") val necesitaRecoleccion: Boolean = false,
    @SerialName("recoleccion_id") val recoleccionId: String? = null,
    @SerialName("recoleccion_fecha") val recoleccionFecha: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = EnvioSolicitud(
        id = id, folio = folio, clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono, estado = estado, userId = userId,
        envioPaqueteria = envioPaqueteria, envioServicio = envioServicio,
        guiaNumero = guiaNumero, guiaUrl = guiaUrl, guiaRastreoUrl = guiaRastreoUrl,
        guiaPaqueteria = guiaPaqueteria, guiaServicio = guiaServicio,
        guiaCosto = guiaCosto, guiaGeneradaEn = guiaGeneradaEn,
        necesitaRecoleccion = necesitaRecoleccion, recoleccionId = recoleccionId,
        recoleccionFecha = recoleccionFecha, createdAt = createdAt,
    )
}

/**
 * Solicitudes que llevan envío a domicilio.
 *
 * Solo lectura: generar la guía y pedir la recolección pasan por las
 * Edge Functions de [EnvioApi], que son las que tienen el token de la
 * paquetería. Desde aquí no se escribe ningún campo de guía.
 */
interface EnvioRepository {
    /** Aún sin guía: lo que queda por despachar. */
    suspend fun pendientesDeGuia(): DataResult<List<EnvioSolicitud>>

    /** Con guía ya generada, lo más reciente primero. */
    suspend fun conGuia(limite: Int = 100): DataResult<List<EnvioSolicitud>>

    suspend fun porId(id: String): DataResult<EnvioSolicitud>
}

@Singleton
class EnvioRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : EnvioRepository {

    private companion object {
        const val TABLE = "order_requests"
        val COLUMNAS = Columns.list(
            "id", "folio", "cliente_nombre", "cliente_telefono", "estado", "user_id",
            "envio_paqueteria", "envio_servicio",
            "guia_numero", "guia_url", "guia_rastreo_url", "guia_paqueteria",
            "guia_servicio", "guia_costo", "guia_generada_en",
            "necesita_recoleccion", "recoleccion_id", "recoleccion_fecha",
            "created_at",
        )
    }

    override suspend fun pendientesDeGuia(): DataResult<List<EnvioSolicitud>> =
        withContext(io) {
            safeSupabaseCall("pendientesDeGuia") {
                postgrest.from(TABLE).select(COLUMNAS) {
                    filter {
                        // Solo las de domicilio: una entrega en persona no
                        // lleva guía y la función la rechazaría.
                        eq("metodo_entrega", "domicilio")
                        // Rechazadas y canceladas no se despachan.
                        isIn("estado", listOf("enviada", "atendida"))
                    }
                    order("created_at", Order.ASCENDING)
                    limit(100)
                }.decodeList<EnvioSolicitudDto>()
            }.map { list ->
                // El «sin guía» se filtra aquí y no en la consulta: el
                // conjunto ya viene acotado por estado y método, y así no
                // dependemos de cómo exprese PostgREST un `is null`.
                list.filter { it.guiaNumero.isNullOrBlank() }.map { it.toDomain() }
            }
        }

    override suspend fun conGuia(limite: Int): DataResult<List<EnvioSolicitud>> =
        withContext(io) {
            safeSupabaseCall("conGuia") {
                postgrest.from(TABLE).select(COLUMNAS) {
                    // Las que tienen guía primero: las sin generar quedan
                    // al final y se descartan abajo.
                    order(
                        "guia_generada_en",
                        Order.DESCENDING,
                        nullsFirst = false,
                    )
                    limit(limite.toLong())
                }.decodeList<EnvioSolicitudDto>()
            }.map { list ->
                list.filterNot { it.guiaNumero.isNullOrBlank() }.map { it.toDomain() }
            }
        }

    override suspend fun porId(id: String): DataResult<EnvioSolicitud> = withContext(io) {
        safeSupabaseCall("envioPorId") {
            postgrest.from(TABLE).select(COLUMNAS) {
                filter { eq("id", id) }
            }.decodeSingle<EnvioSolicitudDto>()
        }.map { it.toDomain() }
    }
}
