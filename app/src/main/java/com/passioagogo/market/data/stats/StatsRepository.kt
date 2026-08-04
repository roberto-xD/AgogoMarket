package com.passioagogo.market.data.stats

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

/**
 * Lecturas de las vistas estadísticas (11_views.sql). Todas son
 * security_invoker: el admin ve todo, un vendedor solo su tienda.
 */

@Serializable
data class VentaDiariaDto(
    val fecha: String,
    @SerialName("location_id") val locationId: String,
    val tienda: String,
    val pedidos: Long,
    val subtotal: Double,
    val descuentos: Double,
    val envios: Double,
    val total: Double,
    @SerialName("ticket_promedio") val ticketPromedio: Double,
)

@Serializable
data class TopProductoDto(
    @SerialName("variant_id") val variantId: String,
    val sku: String,
    val producto: String,
    val categoria: String,
    val unidades: Long,
    val ingreso: Double,
    val costo: Double,
    val utilidad: Double,
    @SerialName("margen_pct") val margenPct: Double? = null,
)

@Serializable
data class VentasTiendaDto(
    @SerialName("location_id") val locationId: String,
    val tienda: String,
    val pedidos: Long,
    val unidades: Long,
    val ingreso: Double,
    val utilidad: Double,
    @SerialName("margen_pct") val margenPct: Double? = null,
)

@Serializable
data class VentasVendedorDto(
    @SerialName("vendedor_id") val vendedorId: String? = null,
    val vendedor: String? = null,
    val tienda: String,
    val pedidos: Long,
    val unidades: Long,
    val ingreso: Double,
    val utilidad: Double,
)

@Serializable
data class SaldoPendienteDto(
    @SerialName("order_id") val orderId: String,
    val folio: Long,
    @SerialName("location_id") val locationId: String,
    val cliente: String? = null,
    val estado: String,
    val total: Double,
    val pagado: Double,
    val saldo: Double,
)

interface StatsRepository {
    /** [desde] en formato yyyy-MM-dd; null = histórico completo. */
    suspend fun getVentasDiarias(desde: String? = null): DataResult<List<VentaDiariaDto>>
    suspend fun getTopProductos(limit: Int = 30): DataResult<List<TopProductoDto>>
    suspend fun getVentasPorTienda(): DataResult<List<VentasTiendaDto>>
    suspend fun getVentasPorVendedor(): DataResult<List<VentasVendedorDto>>
    suspend fun getSaldosPendientes(): DataResult<List<SaldoPendienteDto>>
}

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : StatsRepository {

    override suspend fun getVentasDiarias(desde: String?): DataResult<List<VentaDiariaDto>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from("v_ventas_diarias").select {
                    filter { desde?.let { gte("fecha", it) } }
                    order("fecha", Order.DESCENDING)
                }.decodeList<VentaDiariaDto>()
            }
        }

    override suspend fun getTopProductos(limit: Int): DataResult<List<TopProductoDto>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from("v_top_productos").select {
                    limit(limit.toLong())
                }.decodeList<TopProductoDto>()
            }
        }

    override suspend fun getVentasPorTienda(): DataResult<List<VentasTiendaDto>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from("v_ventas_por_tienda").select()
                    .decodeList<VentasTiendaDto>()
            }
        }

    override suspend fun getVentasPorVendedor(): DataResult<List<VentasVendedorDto>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from("v_ventas_por_vendedor").select()
                    .decodeList<VentasVendedorDto>()
            }
        }

    override suspend fun getSaldosPendientes(): DataResult<List<SaldoPendienteDto>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from("v_saldos_pendientes").select {
                    order("folio", Order.DESCENDING)
                }.decodeList<SaldoPendienteDto>()
            }
        }
}
