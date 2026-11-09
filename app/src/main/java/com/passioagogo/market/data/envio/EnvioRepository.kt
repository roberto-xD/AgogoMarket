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
    /**
     * Quién levantó la solicitud.
     *
     * Si fue el propio cliente, su dirección cifrada está bajo este id.
     * Si fue un promotor, este id es el del promotor y NO hay dirección
     * cifrada del destinatario: el promotor captura nombre y teléfono,
     * no domicilio.
     */
    val createdBy: String,
    /** Rol de quien la levantó; decide si hay dirección cifrada. */
    val creadorRol: String?,
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

    /**
     * Solo las del propio cliente tienen dirección cifrada: un promotor
     * captura nombre y teléfono, no domicilio. Generar una guía con el
     * sobre del promotor mandaría el paquete a su casa.
     */
    val tieneDireccionCifrada: Boolean get() = creadorRol == "cliente"

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
    val estado: String? = null,
    @SerialName("created_by") val createdBy: String,
    /** La vista ya resuelve el rol de quien la levantó. */
    @SerialName("creador_rol") val creadorRol: String? = null,
    @SerialName("envio_paqueteria") val envioPaqueteria: String? = null,
    @SerialName("envio_servicio") val envioServicio: String? = null,
    @SerialName("guia_numero") val guiaNumero: String? = null,
    @SerialName("guia_url") val guiaUrl: String? = null,
    @SerialName("guia_rastreo_url") val guiaRastreoUrl: String? = null,
    @SerialName("guia_paqueteria") val guiaPaqueteria: String? = null,
    @SerialName("guia_servicio") val guiaServicio: String? = null,
    @SerialName("guia_costo") val guiaCosto: Double? = null,
    @SerialName("guia_generada_en") val guiaGeneradaEn: String? = null,
    /**
     * Nullable a propósito: la columna admite nulos y un `= false` solo
     * cubre que la clave FALTE, no que llegue como null explícito.
     */
    @SerialName("necesita_recoleccion") val necesitaRecoleccion: Boolean? = null,
    @SerialName("recoleccion_id") val recoleccionId: String? = null,
    @SerialName("recoleccion_fecha") val recoleccionFecha: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = EnvioSolicitud(
        id = id, folio = folio, clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono, estado = estado.orEmpty(),
        createdBy = createdBy,
        creadorRol = creadorRol,
        envioPaqueteria = envioPaqueteria, envioServicio = envioServicio,
        guiaNumero = guiaNumero, guiaUrl = guiaUrl, guiaRastreoUrl = guiaRastreoUrl,
        guiaPaqueteria = guiaPaqueteria, guiaServicio = guiaServicio,
        guiaCosto = guiaCosto, guiaGeneradaEn = guiaGeneradaEn,
        necesitaRecoleccion = necesitaRecoleccion ?: false,
        recoleccionId = recoleccionId,
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
        /**
         * Vista de administración, no la tabla.
         *
         * El script 47 quitó a `authenticated` el permiso sobre
         * `order_requests` y repuso solo las columnas del cliente: el PDF
         * de la guía y su costo quedaron fuera a propósito. El admin usa
         * el mismo rol, así que lee por una vista que corre con los
         * permisos de su dueño y filtra por `is_admin()`.
         */
        const val TABLE = "v_envios_admin"
        // Sin embebidos: la vista ya trae el rol del creador resuelto.
        val COLUMNAS = Columns.raw(
            "id, folio, cliente_nombre, cliente_telefono, estado, created_by, " +
                "envio_paqueteria, envio_servicio, " +
                "guia_numero, guia_url, guia_rastreo_url, guia_paqueteria, " +
                "guia_servicio, guia_costo, guia_generada_en, " +
                "necesita_recoleccion, recoleccion_id, recoleccion_fecha, created_at, " +
                "creador_rol"
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
                        // Solo las abiertas: una atendida ya se convirtió en
                        // pedido y su envío se gestiona desde ahí, no aquí.
                        eq("estado", "enviada")
                    }
                    order("created_at", Order.ASCENDING)
                    limit(100)
                }.decodeList<EnvioSolicitudDto>()
            }.map { list ->
                // El «sin guía» se filtra aquí y no en la consulta: el
                // conjunto ya viene acotado por estado y método, y así no
                // dependemos de cómo exprese PostgREST un `is null`.
                list.map { it.toDomain() }
                    .filter { it.guiaNumero.isNullOrBlank() }
                    // Solo las del cliente: las de promotor no tienen
                    // dirección cifrada y quedan fuera hasta que el
                    // esquema guarde el destinatario por separado.
                    .filter { it.tieneDireccionCifrada }
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
