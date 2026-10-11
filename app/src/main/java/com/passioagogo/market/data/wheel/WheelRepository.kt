package com.passioagogo.market.data.wheel

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

// ============ Dominio ============

data class WheelPrize(
    val id: String,
    val rotulo: String,
    val detalle: String?,
    /** Probabilidad relativa; el reparto es peso / suma de pesos. */
    val peso: Int,
    /** false = sector de consolación, no hay nada que reclamar. */
    val otorgaPremio: Boolean,
    val orden: Int,
    val activo: Boolean,
)

data class WheelPrizeDraft(
    val rotulo: String,
    val detalle: String?,
    val peso: Int,
    val otorgaPremio: Boolean,
    val orden: Int,
)

data class WheelSpin(
    val id: String,
    val codigo: String,
    val rotulo: String,
    val otorgaPremio: Boolean,
    val canjeado: Boolean,
    val canjeadoAt: String?,
    val createdAt: String?,
)

data class WheelTerm(
    val id: String,
    val titulo: String,
    val cuerpo: String,
    val orden: Int,
    val activo: Boolean,
) {
    /** El borrador sembrado deja huecos entre corchetes por rellenar. */
    val tienePendientes: Boolean
        get() = Regex("\\[[^\\]]+\\]").containsMatchIn(cuerpo)
}

// ============ DTOs ============

@Serializable
data class WheelPrizeDto(
    val id: String,
    val rotulo: String,
    val detalle: String? = null,
    val peso: Int = 1,
    @SerialName("otorga_premio") val otorgaPremio: Boolean = true,
    val orden: Int = 0,
    val activo: Boolean = true,
) {
    fun toDomain() = WheelPrize(
        id = id, rotulo = rotulo, detalle = detalle, peso = peso,
        otorgaPremio = otorgaPremio, orden = orden, activo = activo,
    )
}

@Serializable
data class NewWheelPrizeDto(
    val rotulo: String,
    val detalle: String? = null,
    val peso: Int = 1,
    @SerialName("otorga_premio") val otorgaPremio: Boolean = true,
    val orden: Int = 0,
)

@Serializable
data class WheelSpinDto(
    val id: String,
    val codigo: String,
    val rotulo: String,
    @SerialName("otorga_premio") val otorgaPremio: Boolean = true,
    val canjeado: Boolean = false,
    @SerialName("canjeado_at") val canjeadoAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toDomain() = WheelSpin(
        id = id, codigo = codigo, rotulo = rotulo, otorgaPremio = otorgaPremio,
        canjeado = canjeado, canjeadoAt = canjeadoAt, createdAt = createdAt,
    )
}

@Serializable
data class WheelTermDto(
    val id: String,
    val titulo: String,
    val cuerpo: String,
    val orden: Int = 0,
    val activo: Boolean = true,
) {
    fun toDomain() = WheelTerm(
        id = id, titulo = titulo, cuerpo = cuerpo, orden = orden, activo = activo,
    )
}

@Serializable
data class SiteSettingDto(val clave: String, val valor: String)

// ============ Repositorio ============

/**
 * Administración de la ruleta de descuentos.
 *
 * Los giros NO se crean desde aquí: los emite la Edge Function `spin`
 * con service_role, y `wheel_spins` no tiene política de INSERT a
 * propósito. Desde la app solo se consultan y se marcan como canjeados.
 */
interface WheelRepository {
    // -- Premios --
    suspend fun getPrizes(): DataResult<List<WheelPrize>>
    suspend fun createPrize(draft: WheelPrizeDraft): DataResult<WheelPrize>
    suspend fun updatePrize(prize: WheelPrize): DataResult<WheelPrize>
    suspend fun deletePrize(id: String): DataResult<Unit>

    // -- Códigos emitidos --
    suspend fun getSpins(pendientesOnly: Boolean = false): DataResult<List<WheelSpin>>
    /** Verifica un código reclamado por WhatsApp. */
    suspend fun buscarCodigo(codigo: String): DataResult<WheelSpin?>
    suspend fun marcarCanjeado(id: String, canjeado: Boolean): DataResult<Unit>

    // -- Bases legales --
    suspend fun getTerms(): DataResult<List<WheelTerm>>
    suspend fun updateTerm(term: WheelTerm): DataResult<WheelTerm>

    // -- Interruptor de la sección en la web --
    suspend fun isRuletaVisible(): DataResult<Boolean>
    suspend fun setRuletaVisible(visible: Boolean): DataResult<Unit>
}

@Singleton
class WheelRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : WheelRepository {

    private companion object {
        const val PRIZES = "wheel_prizes"
        const val SPINS = "wheel_spins"
        const val TERMS = "wheel_terms"
        const val SETTINGS = "site_settings"
        const val CLAVE_RULETA = "ruleta_visible"
    }

    // ---------- Premios ----------

    override suspend fun getPrizes(): DataResult<List<WheelPrize>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(PRIZES).select {
                order("orden", Order.ASCENDING)
            }.decodeList<WheelPrizeDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createPrize(draft: WheelPrizeDraft): DataResult<WheelPrize> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PRIZES).insert(
                    NewWheelPrizeDto(
                        rotulo = draft.rotulo,
                        detalle = draft.detalle,
                        peso = draft.peso,
                        otorgaPremio = draft.otorgaPremio,
                        orden = draft.orden,
                    )
                ) { select() }.decodeSingle<WheelPrizeDto>()
            }.map { it.toDomain() }
        }

    override suspend fun updatePrize(prize: WheelPrize): DataResult<WheelPrize> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PRIZES).update({
                    set("rotulo", prize.rotulo)
                    set("detalle", prize.detalle)
                    set("peso", prize.peso)
                    set("otorga_premio", prize.otorgaPremio)
                    set("orden", prize.orden)
                    set("activo", prize.activo)
                }) {
                    select()
                    filter { eq("id", prize.id) }
                }.decodeSingle<WheelPrizeDto>()
            }.map { it.toDomain() }
        }

    override suspend fun deletePrize(id: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            // Borrado lógico: wheel_spins referencia el premio y los
            // códigos emitidos deben seguir diciendo qué se ganó.
            postgrest.from(PRIZES).update({ set("activo", false) }) {
                filter { eq("id", id) }
            }
            Unit
        }
    }

    // ---------- Códigos ----------

    override suspend fun getSpins(pendientesOnly: Boolean): DataResult<List<WheelSpin>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(SPINS).select {
                    filter {
                        // Los sectores de consolación no se reclaman
                        eq("otorga_premio", true)
                        if (pendientesOnly) eq("canjeado", false)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(200)
                }.decodeList<WheelSpinDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun buscarCodigo(codigo: String): DataResult<WheelSpin?> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(SPINS).select {
                    // Sin distinción de mayúsculas: el código llega dictado
                    // por WhatsApp y se teclea como salga.
                    filter { ilike("codigo", codigo.trim()) }
                }.decodeList<WheelSpinDto>().firstOrNull()?.toDomain()
            }
        }

    override suspend fun marcarCanjeado(id: String, canjeado: Boolean): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(SPINS).update({
                    set("canjeado", canjeado)
                    // "now" es un valor especial que PostgreSQL acepta como
                    // entrada de timestamp; "now()" como texto no lo es.
                    set("canjeado_at", if (canjeado) "now" else null)
                }) { filter { eq("id", id) } }
                Unit
            }
        }

    // ---------- Bases ----------

    override suspend fun getTerms(): DataResult<List<WheelTerm>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TERMS).select {
                order("orden", Order.ASCENDING)
            }.decodeList<WheelTermDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateTerm(term: WheelTerm): DataResult<WheelTerm> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TERMS).update({
                set("titulo", term.titulo)
                set("cuerpo", term.cuerpo)
                set("orden", term.orden)
                set("activo", term.activo)
            }) {
                select()
                filter { eq("id", term.id) }
            }.decodeSingle<WheelTermDto>()
        }.map { it.toDomain() }
    }

    // ---------- Interruptor ----------

    override suspend fun isRuletaVisible(): DataResult<Boolean> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(SETTINGS).select {
                filter { eq("clave", CLAVE_RULETA) }
            }.decodeList<SiteSettingDto>()
                .firstOrNull()?.valor?.equals("true", ignoreCase = true) ?: false
        }
    }

    override suspend fun setRuletaVisible(visible: Boolean): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(SETTINGS).update({
                set("valor", if (visible) "true" else "false")
            }) { filter { eq("clave", CLAVE_RULETA) } }
            Unit
        }
    }
}
