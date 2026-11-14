package com.passioagogo.market.data.codigos

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Qué se canjea con el código. */
enum class TipoCodigo(val sql: String, val etiqueta: String) {
    DESCUENTO("descuento", "Descuento"),
    REGALO("regalo", "Regalo"),
    ENVIO_GRATIS("envio_gratis", "Envío gratis");

    companion object {
        fun desde(valor: String?): TipoCodigo =
            entries.firstOrNull { it.sql == valor } ?: DESCUENTO
    }
}

/**
 * Un código canjeable, venga de la ruleta o repartido a mano.
 *
 * `wheel_spins` guarda quién giró; esta tabla guarda lo que se canjea.
 * Se enlazan por el código.
 */
data class Codigo(
    val id: String,
    val codigo: String,
    val tipo: TipoCodigo,
    /** Solo con descuento, de 1 a 100. */
    val descuentoPct: Int?,
    /** Solo con regalo: qué se mete en la caja. */
    val regalo: String?,
    /** El «a partir de $X», sobre artículos y nunca sobre el envío. */
    val minimoCompra: Double?,
    val usosMax: Int,
    val usos: Int,
    val expiraAt: String?,
    val activo: Boolean,
    /** ruleta o manual. */
    val origen: String,
    /**
     * Escondido de la lista. NO afecta al canje: un código oculto y
     * activo se sigue canjeando, porque lo repartido no deja de valer
     * porque administración limpie su pantalla.
     */
    val oculto: Boolean,
    val nota: String?,
    val createdAt: String?,
) {
    val agotado: Boolean get() = usos >= usosMax

    /**
     * Caducado según la fecha. Se compara como texto ISO, que ordena
     * igual que la fecha y evita parsear en cada fila de la lista.
     */
    fun caducado(ahoraIso: String): Boolean =
        expiraAt != null && expiraAt < ahoraIso

    fun canjeable(ahoraIso: String): Boolean =
        activo && !agotado && !caducado(ahoraIso)

    /** Lo que resume el código en una línea. */
    val detalle: String
        get() = when (tipo) {
            TipoCodigo.DESCUENTO -> "${descuentoPct ?: 0}% de descuento"
            TipoCodigo.REGALO -> regalo.orEmpty().ifBlank { "Regalo" }
            TipoCodigo.ENVIO_GRATIS -> "Envío gratis" +
                (minimoCompra?.let { " desde $${it.toInt()}" } ?: "")
        }
}

/** Lo que se captura al crear uno. */
data class CodigoDraft(
    val codigo: String,
    val tipo: TipoCodigo,
    val descuentoPct: Int? = null,
    val regalo: String? = null,
    val minimoCompra: Double? = null,
    val usosMax: Int = 1,
    val expiraAt: String? = null,
    val nota: String? = null,
)

@Serializable
data class CodigoDto(
    val id: String,
    val codigo: String,
    val tipo: String = "descuento",
    @SerialName("descuento_pct") val descuentoPct: Int? = null,
    val regalo: String? = null,
    @SerialName("minimo_compra") val minimoCompra: Double? = null,
    @SerialName("usos_max") val usosMax: Int = 1,
    val usos: Int = 0,
    @SerialName("expira_at") val expiraAt: String? = null,
    val activo: Boolean = true,
    val origen: String = "manual",
    val oculto: Boolean = false,
    val nota: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = Codigo(
        id = id, codigo = codigo, tipo = TipoCodigo.desde(tipo),
        descuentoPct = descuentoPct, regalo = regalo, minimoCompra = minimoCompra,
        usosMax = usosMax, usos = usos, expiraAt = expiraAt, activo = activo,
        origen = origen, oculto = oculto, nota = nota, createdAt = createdAt,
    )
}

@Serializable
data class NuevoCodigoDto(
    val codigo: String,
    val tipo: String,
    @SerialName("descuento_pct") val descuentoPct: Int? = null,
    val regalo: String? = null,
    @SerialName("minimo_compra") val minimoCompra: Double? = null,
    @SerialName("usos_max") val usosMax: Int,
    @SerialName("expira_at") val expiraAt: String? = null,
    val nota: String? = null,
    val origen: String = "manual",
)

/**
 * Gestión de códigos desde la app.
 *
 * Accede a la tabla directamente: su RLS solo deja entrar a
 * administración, así que no hace falta una función aparte.
 */
interface CodigoRepository {
    suspend fun getCodigos(
        soloManuales: Boolean = false,
        incluirOcultos: Boolean = false,
    ): DataResult<List<Codigo>>
    suspend fun crear(draft: CodigoDraft): DataResult<Codigo>
    /** Apaga o enciende uno sin borrarlo: cortar una campaña a media marcha. */
    suspend fun cambiarActivo(id: String, activo: Boolean): DataResult<Unit>

    /**
     * Borrado lógico: lo saca de la lista sin tocar la base.
     * El código sigue canjeándose si estaba activo.
     */
    suspend fun ocultar(id: String, oculto: Boolean = true): DataResult<Unit>

    /**
     * Cambia lo que se puede cambiar sin romper lo ya prometido.
     *
     * El tipo y el porcentaje NO se editan: quien recibió «15%» tiene que
     * seguir recibiendo 15%. Esto solo mueve la caducidad, el tope de usos
     * y la nota.
     */
    suspend fun editar(
        id: String,
        expiraAt: String?,
        usosMax: Int,
        nota: String?,
    ): DataResult<Unit>
}

@Singleton
class CodigoRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CodigoRepository {

    private companion object { const val TABLE = "codigos" }

    override suspend fun getCodigos(
        soloManuales: Boolean,
        incluirOcultos: Boolean,
    ): DataResult<List<Codigo>> = withContext(io) {
        safeSupabaseCall("getCodigos") {
            postgrest.from(TABLE).select {
                filter {
                    if (soloManuales) eq("origen", "manual")
                    // Lo oculto se filtra aquí, no en el cliente: con 200
                    // filas de tope, traerlo para descartarlo dejaría la
                    // lista corta sin que se note por qué.
                    if (!incluirOcultos) eq("oculto", false)
                }
                order("created_at", Order.DESCENDING)
                limit(200)
            }.decodeList<CodigoDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun crear(draft: CodigoDraft): DataResult<Codigo> = withContext(io) {
        safeSupabaseCall("crearCodigo") {
            postgrest.from(TABLE).insert(
                NuevoCodigoDto(
                    // Siempre en mayúsculas: se dictan por teléfono, y el
                    // CHECK de la tabla lo exige.
                    codigo = draft.codigo.trim().uppercase(),
                    tipo = draft.tipo.sql,
                    descuentoPct = draft.descuentoPct,
                    regalo = draft.regalo?.trim()?.ifBlank { null },
                    minimoCompra = draft.minimoCompra,
                    usosMax = draft.usosMax,
                    expiraAt = draft.expiraAt,
                    nota = draft.nota?.trim()?.ifBlank { null },
                )
            ) { select() }.decodeSingle<CodigoDto>()
        }.map { it.toDomain() }
    }

    override suspend fun ocultar(id: String, oculto: Boolean): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall("ocultarCodigo") {
                postgrest.from(TABLE).update({ set("oculto", oculto) }) {
                    filter { eq("id", id) }
                }
                Unit
            }
        }

    override suspend fun editar(
        id: String,
        expiraAt: String?,
        usosMax: Int,
        nota: String?,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall("editarCodigo") {
            postgrest.from(TABLE).update({
                set("expira_at", expiraAt)
                set("usos_max", usosMax)
                set("nota", nota?.trim()?.ifBlank { null })
            }) { filter { eq("id", id) } }
            Unit
        }
    }

    override suspend fun cambiarActivo(id: String, activo: Boolean): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall("cambiarActivoCodigo") {
                postgrest.from(TABLE).update({ set("activo", activo) }) {
                    filter { eq("id", id) }
                }
                Unit
            }
        }
}
