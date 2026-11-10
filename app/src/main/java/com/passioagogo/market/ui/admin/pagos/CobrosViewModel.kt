package com.passioagogo.market.ui.admin.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.pagos.Cobro
import com.passioagogo.market.data.pagos.PagosRepository
import com.passioagogo.market.data.pagos.PorReembolsar
import com.passioagogo.market.data.pagos.Reembolso
import com.passioagogo.market.data.pagos.ReembolsoApi
import com.passioagogo.market.data.pagos.ResultadoReembolso
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Periodos habituales al cuadrar: no hace falta un calendario. */
enum class Periodo(val etiqueta: String) {
    MES_ACTUAL("Este mes"),
    MES_ANTERIOR("Mes pasado"),
    TODO("Todo"),
}

data class CobrosUiState(
    val cobros: List<Cobro> = emptyList(),
    val periodo: Periodo = Periodo.MES_ACTUAL,
    val soloAprobadas: Boolean = true,
    val isLoading: Boolean = false,
    val detalle: Cobro? = null,
    // ---- Devoluciones ----
    val porReembolsar: List<PorReembolsar> = emptyList(),
    /** Solicitud cuya devolución se está confirmando. */
    val reembolsando: PorReembolsar? = null,
    val procesandoReembolso: Boolean = false,
    val reembolsoHecho: Reembolso? = null,
    val errorMessage: String? = null,
) {
    val cobrado: Double get() = cobros.sumOf { it.pagoTotal ?: 0.0 }
    val comisiones: Double get() = cobros.sumOf { it.pagoComision ?: 0.0 }
    val guias: Double get() = cobros.sumOf { it.guiaCosto ?: 0.0 }

    /**
     * Suma solo lo que ya tiene neto conocido.
     *
     * Las ventas cuyo neto aún no informa la pasarela quedan fuera en
     * vez de contar como cero: un cero diría que esa venta no dejó nada.
     */
    val netoTrasGuia: Double get() = cobros.sumOf { it.netoTrasGuia ?: 0.0 }

    val sinNetoTodavia: Int get() = cobros.count { it.aprobado && it.pagoNeto == null }

    val ventas: Int get() = cobros.size

    /** Dinero aprobado que la pasarela aún no ha soltado. */
    fun porLiberar(ahoraIso: String): Double =
        cobros.filter { it.pendienteDeLiberar(ahoraIso) }.sumOf { it.pagoNeto ?: 0.0 }
}

@HiltViewModel
class CobrosViewModel @Inject constructor(
    private val repository: PagosRepository,
    private val reembolsoApi: ReembolsoApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CobrosUiState())
    val uiState: StateFlow<CobrosUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onPeriodo(p: Periodo) {
        _uiState.update { it.copy(periodo = p) }
        refresh()
    }

    fun onSoloAprobadas(v: Boolean) {
        _uiState.update { it.copy(soloAprobadas = v) }
        refresh()
    }

    // ---- Devoluciones ----

    fun onPedirReembolso(item: PorReembolsar) =
        _uiState.update { it.copy(reembolsando = item) }

    fun onCancelarReembolso() = _uiState.update { it.copy(reembolsando = null) }
    fun onCerrarReembolsoHecho() = _uiState.update { it.copy(reembolsoHecho = null) }

    /**
     * Devuelve el dinero.
     *
     * [monto] null devuelve todo. Una parcial no cancela la solicitud:
     * una compra devuelta a medias sigue en pie.
     */
    fun onReembolsar(
        item: PorReembolsar,
        monto: Double? = null,
        nota: String? = null,
        devolverCodigo: Boolean = true,
    ) {
        _uiState.update { it.copy(reembolsando = null, procesandoReembolso = true) }
        viewModelScope.launch {
            when (val r = reembolsoApi.reembolsar(item.id, monto, nota, devolverCodigo)) {
                is ResultadoReembolso.Ok -> {
                    _uiState.update {
                        it.copy(procesandoReembolso = false, reembolsoHecho = r.reembolso)
                    }
                    refresh()
                }
                is ResultadoReembolso.Fallo -> _uiState.update {
                    it.copy(procesandoReembolso = false, errorMessage = r.mensaje)
                }
            }
        }
    }

    fun onVerDetalle(cobro: Cobro) = _uiState.update { it.copy(detalle = cobro) }
    fun onCerrarDetalle() = _uiState.update { it.copy(detalle = null) }
    fun onCerrarError() = _uiState.update { it.copy(errorMessage = null) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val (desde, hasta) = rango(_uiState.value.periodo)
            val r = repository.getCobros(
                desde = desde,
                hasta = hasta,
                soloAprobadas = _uiState.value.soloAprobadas,
                limite = 500,
            )
            val cola = reembolsoApi.porReembolsar()

            _uiState.update { estado ->
                estado.copy(
                    isLoading = false,
                    cobros = (r as? DataResult.Success)?.data ?: estado.cobros,
                    porReembolsar = (cola as? DataResult.Success)?.data
                        ?: estado.porReembolsar,
                    errorMessage = (r as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    /** Fechas en AAAA-MM-DD; la función las interpreta en hora de CDMX. */
    private fun rango(periodo: Periodo): Pair<String?, String?> {
        if (periodo == Periodo.TODO) return null to null
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        if (periodo == Periodo.MES_ANTERIOR) cal.add(Calendar.MONTH, -1)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
        val desde = fmt.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val hasta = fmt.format(cal.time)
        return desde to hasta
    }
}
