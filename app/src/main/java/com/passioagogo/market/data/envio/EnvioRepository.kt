package com.passioagogo.market.data.envio

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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Una solicitud desde la óptica del envío.
 *
 * No trae la dirección: esa vive cifrada y solo se abre en el momento
 * de generar la guía, con la clave privada del administrador.
 */
data class EnvioSolicitud(
    val id: String,
    /** Interno: ordena y es con el que está escrito el historial. */
    val folio: Long,
    /** Lo que el cliente dicta por WhatsApp. */
    val codigoPedido: String,
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
    /** El cliente pidió cancelar y el dinero sigue cobrado. */
    val cancelacionSolicitadaAt: String?,
    /** Cancelada de verdad: sin pago, o con el reembolso hecho. */
    val canceladaAt: String?,
    val canceladaPor: String?,
    val cancelacionMotivo: String?,
    val createdAt: String?,
) {
    /** Ni se despacha ni se cobra: está fuera de juego. */
    val cancelada: Boolean
        get() = canceladaAt != null || cancelacionSolicitadaAt != null
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
    val folio: Long = 0,
    @SerialName("codigo_pedido") val codigoPedido: String = "",
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
    @SerialName("cancelacion_solicitada_at") val cancelacionSolicitadaAt: String? = null,
    @SerialName("cancelada_at") val canceladaAt: String? = null,
    @SerialName("cancelada_por") val canceladaPor: String? = null,
    @SerialName("cancelacion_motivo") val cancelacionMotivo: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = EnvioSolicitud(
        id = id, folio = folio, codigoPedido = codigoPedido,
        clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono, estado = estado.orEmpty(),
        createdBy = createdBy,
        creadorRol = creadorRol,
        envioPaqueteria = envioPaqueteria, envioServicio = envioServicio,
        guiaNumero = guiaNumero, guiaUrl = guiaUrl, guiaRastreoUrl = guiaRastreoUrl,
        guiaPaqueteria = guiaPaqueteria, guiaServicio = guiaServicio,
        guiaCosto = guiaCosto, guiaGeneradaEn = guiaGeneradaEn,
        necesitaRecoleccion = necesitaRecoleccion ?: false,
        recoleccionId = recoleccionId,
        recoleccionFecha = recoleccionFecha,
        cancelacionSolicitadaAt = cancelacionSolicitadaAt, canceladaAt = canceladaAt,
        canceladaPor = canceladaPor, cancelacionMotivo = cancelacionMotivo,
        createdAt = createdAt,
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

    /**
     * Busca por el código que dicta el cliente.
     * Sin esto, «mi pedido K7MD» no se puede localizar.
     */
    suspend fun buscarPorCodigo(codigo: String): DataResult<List<EnvioSolicitud>>
}

@Singleton
class EnvioRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : EnvioRepository {

    private companion object {
        /**
         * Función de administración, no la tabla.
         *
         * El script 47 quitó a `authenticated` el permiso sobre
         * `order_requests` y repuso solo las columnas del cliente: el PDF
         * de la guía, su costo y la recolección quedaron fuera a
         * propósito. El admin usa el mismo rol, así que lee por una
         * función SECURITY DEFINER que comprueba `is_admin()` y lanza si
         * no lo es.
         */
        const val RPC = "fn_envios_admin"
    }

    override suspend fun pendientesDeGuia(): DataResult<List<EnvioSolicitud>> =
        withContext(io) {
            safeSupabaseCall("pendientesDeGuia") {
                // El filtro vive en la función: domicilio, abierta, sin
                // guía y levantada por el cliente.
                postgrest.rpc(
                    RPC,
                    buildJsonObject {
                        put("p_solo_sin_guia", true)
                        put("p_limite", 100)
                    },
                ).decodeList<EnvioSolicitudDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun conGuia(limite: Int): DataResult<List<EnvioSolicitud>> =
        withContext(io) {
            safeSupabaseCall("conGuia") {
                postgrest.rpc(
                    RPC,
                    buildJsonObject { put("p_limite", limite) },
                ).decodeList<EnvioSolicitudDto>()
            }.map { list ->
                list.map { it.toDomain() }.filterNot { it.guiaNumero.isNullOrBlank() }
            }
        }

    override suspend fun buscarPorCodigo(
        codigo: String,
    ): DataResult<List<EnvioSolicitud>> = withContext(io) {
        safeSupabaseCall("buscarPorCodigo") {
            // La función normaliza mayúsculas y espacios: llega copiado
            // de WhatsApp y llega como llegue.
            postgrest.rpc(
                RPC,
                buildJsonObject { put("p_codigo", codigo) },
            ).decodeList<EnvioSolicitudDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun porId(id: String): DataResult<EnvioSolicitud> = withContext(io) {
        safeSupabaseCall("envioPorId") {
            postgrest.rpc(
                RPC,
                buildJsonObject { put("p_id", id) },
            ).decodeList<EnvioSolicitudDto>().first()
        }.map { it.toDomain() }
    }
}
