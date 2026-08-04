package com.passioagogo.market.ui.admin.stats

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.stats.SaldoPendienteDto
import com.passioagogo.market.data.stats.StatsRepository
import com.passioagogo.market.data.stats.TopProductoDto
import com.passioagogo.market.data.stats.VentaDiariaDto
import com.passioagogo.market.data.stats.VentasTiendaDto
import com.passioagogo.market.data.stats.VentasVendedorDto
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

enum class Reporte(val etiqueta: String) {
    VENTAS_DIARIAS("Ventas diarias"),
    TOP_PRODUCTOS("Top productos"),
    POR_TIENDA("Por tienda"),
    POR_VENDEDOR("Por vendedor"),
    SALDOS("Saldos"),
}

enum class Rango(val etiqueta: String, val dias: Int?) {
    SIETE("7 días", 7),
    TREINTA("30 días", 30),
    TODO("Todo", null),
}

data class StatsUiState(
    val reporte: Reporte = Reporte.VENTAS_DIARIAS,
    val rango: Rango = Rango.SIETE,
    val ventasDiarias: List<VentaDiariaDto> = emptyList(),
    val topProductos: List<TopProductoDto> = emptyList(),
    val porTienda: List<VentasTiendaDto> = emptyList(),
    val porVendedor: List<VentasVendedorDto> = emptyList(),
    val saldos: List<SaldoPendienteDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun onReporte(reporte: Reporte) {
        _uiState.update { it.copy(reporte = reporte) }
        load()
    }

    fun onRango(rango: Rango) {
        _uiState.update { it.copy(rango = rango) }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val state = _uiState.value
            when (state.reporte) {
                Reporte.VENTAS_DIARIAS -> {
                    val desde = state.rango.dias?.let { dias ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.add(Calendar.DAY_OF_YEAR, -dias)
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(cal.time)
                    }
                    apply(statsRepository.getVentasDiarias(desde)) {
                        copy(ventasDiarias = it)
                    }
                }
                Reporte.TOP_PRODUCTOS ->
                    apply(statsRepository.getTopProductos()) { copy(topProductos = it) }
                Reporte.POR_TIENDA ->
                    apply(statsRepository.getVentasPorTienda()) { copy(porTienda = it) }
                Reporte.POR_VENDEDOR ->
                    apply(statsRepository.getVentasPorVendedor()) { copy(porVendedor = it) }
                Reporte.SALDOS ->
                    apply(statsRepository.getSaldosPendientes()) { copy(saldos = it) }
            }
        }
    }

    private fun <T> apply(
        result: DataResult<List<T>>,
        setter: StatsUiState.(List<T>) -> StatsUiState,
    ) {
        _uiState.update { state ->
            when (result) {
                is DataResult.Success ->
                    state.setter(result.data).copy(isLoading = false)
                is DataResult.Error ->
                    state.copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Reporte.entries.forEach { reporte ->
                FilterChip(
                    selected = state.reporte == reporte,
                    onClick = { viewModel.onReporte(reporte) },
                    label = { Text(reporte.etiqueta) },
                )
            }
        }

        if (state.reporte == Reporte.VENTAS_DIARIAS) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Rango.entries.forEach { rango ->
                    FilterChip(
                        selected = state.rango == rango,
                        onClick = { viewModel.onRango(rango) },
                        label = { Text(rango.etiqueta) },
                    )
                }
            }
        }

        when {
            state.isLoading -> Centered { CircularProgressIndicator() }

            state.errorMessage != null -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }
            }

            else -> when (state.reporte) {
                Reporte.VENTAS_DIARIAS -> VentasDiariasList(state.ventasDiarias)
                Reporte.TOP_PRODUCTOS -> TopProductosList(state.topProductos)
                Reporte.POR_TIENDA -> PorTiendaList(state.porTienda)
                Reporte.POR_VENDEDOR -> PorVendedorList(state.porVendedor)
                Reporte.SALDOS -> SaldosList(state.saldos)
            }
        }
    }
}

@Composable
private fun VentasDiariasList(rows: List<VentaDiariaDto>) {
    if (rows.isEmpty()) return Vacio()
    val totalPeriodo = rows.sumOf { it.total }
    Column {
        Text(
            "Total del periodo: ${moneda.format(totalPeriodo)}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyColumn {
            items(rows, key = { "${it.fecha}-${it.locationId}" }) { row ->
                StatRow(
                    titulo = "${row.fecha}  ·  ${row.tienda}",
                    detalle = "${row.pedidos} pedidos · ticket ${moneda.format(row.ticketPromedio)}",
                    valor = moneda.format(row.total),
                )
            }
        }
    }
}

@Composable
private fun TopProductosList(rows: List<TopProductoDto>) {
    if (rows.isEmpty()) return Vacio()
    LazyColumn {
        items(rows, key = { it.variantId }) { row ->
            StatRow(
                titulo = row.producto,
                detalle = "${row.sku} · ${row.unidades} uds · " +
                    "margen ${row.margenPct?.let { "$it%" } ?: "—"}",
                valor = moneda.format(row.utilidad),
            )
        }
    }
}

@Composable
private fun PorTiendaList(rows: List<VentasTiendaDto>) {
    if (rows.isEmpty()) return Vacio()
    LazyColumn {
        items(rows, key = { it.locationId }) { row ->
            StatRow(
                titulo = row.tienda,
                detalle = "${row.pedidos} pedidos · ${row.unidades} uds · " +
                    "margen ${row.margenPct?.let { "$it%" } ?: "—"}",
                valor = moneda.format(row.ingreso),
            )
        }
    }
}

@Composable
private fun PorVendedorList(rows: List<VentasVendedorDto>) {
    if (rows.isEmpty()) return Vacio()
    LazyColumn {
        items(rows, key = { "${it.vendedorId}-${it.tienda}" }) { row ->
            StatRow(
                titulo = row.vendedor ?: "Sin vendedor",
                detalle = "${row.tienda} · ${row.pedidos} pedidos · ${row.unidades} uds",
                valor = moneda.format(row.ingreso),
            )
        }
    }
}

@Composable
private fun SaldosList(rows: List<SaldoPendienteDto>) {
    if (rows.isEmpty()) return Vacio("Sin saldos pendientes 🎉")
    LazyColumn {
        items(rows, key = { it.orderId }) { row ->
            StatRow(
                titulo = "#${row.folio}  ·  ${row.cliente ?: "Mostrador"}",
                detalle = "Total ${moneda.format(row.total)} · pagado ${moneda.format(row.pagado)}",
                valor = moneda.format(row.saldo),
                valorEnRojo = true,
            )
        }
    }
}

@Composable
private fun StatRow(
    titulo: String,
    detalle: String,
    valor: String,
    valorEnRojo: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (valorEnRojo) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
    }
    HorizontalDivider()
}

@Composable
private fun Vacio(texto: String = "Sin datos para este reporte") {
    Centered {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
