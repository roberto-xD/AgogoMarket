package com.passioagogo.market.ui.admin.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.wheel.WheelPrize
import com.passioagogo.market.data.wheel.WheelPrizeDraft
import com.passioagogo.market.data.wheel.WheelRepository
import com.passioagogo.market.data.wheel.WheelSpin
import com.passioagogo.market.data.wheel.WheelTerm
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WheelUiState(
    val visible: Boolean = false,
    val prizes: List<WheelPrize> = emptyList(),
    val spins: List<WheelSpin> = emptyList(),
    val terms: List<WheelTerm> = emptyList(),
    val soloPendientes: Boolean = true,
    /** Resultado de verificar un código reclamado. */
    val busqueda: String = "",
    val codigoBuscado: WheelSpin? = null,
    val busquedaSinResultado: Boolean = false,
    val editandoPremio: WheelPrize? = null,
    val creandoPremio: Boolean = false,
    val editandoTermino: WheelTerm? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    /** Solo los activos entran en el sorteo. */
    private val pesoTotal: Int get() = prizes.filter { it.activo }.sumOf { it.peso }

    /** Probabilidad real de un premio, para no publicar pesos a ciegas. */
    fun probabilidad(prize: WheelPrize): Double =
        if (!prize.activo || pesoTotal == 0) 0.0
        else prize.peso * 100.0 / pesoTotal

    val pendientes: Int get() = spins.count { !it.canjeado }

    /** Cláusulas con huecos [ ] sin rellenar del borrador sembrado. */
    val terminosPendientes: Int get() = terms.count { it.tienePendientes }
}

@HiltViewModel
class WheelViewModel @Inject constructor(
    private val repository: WheelRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WheelUiState())
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val prizes = repository.getPrizes()
            val spins = repository.getSpins(_uiState.value.soloPendientes)
            val terms = repository.getTerms()
            val visible = repository.isRuletaVisible()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    prizes = (prizes as? DataResult.Success)?.data ?: state.prizes,
                    spins = (spins as? DataResult.Success)?.data ?: state.spins,
                    terms = (terms as? DataResult.Success)?.data ?: state.terms,
                    visible = (visible as? DataResult.Success)?.data ?: state.visible,
                    errorMessage = (prizes as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    // ---------- Interruptor ----------

    fun onToggleVisible(visible: Boolean) {
        _uiState.update { it.copy(visible = visible) }
        viewModelScope.launch {
            val r = repository.setRuletaVisible(visible)
            if (r is DataResult.Error) {
                // Revertir: el estado real es el del servidor
                _uiState.update {
                    it.copy(visible = !visible, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    // ---------- Premios ----------

    fun onNuevoPremio() = _uiState.update { it.copy(creandoPremio = true) }
    fun onEditarPremio(prize: WheelPrize) = _uiState.update { it.copy(editandoPremio = prize) }
    fun onCerrarPremio() =
        _uiState.update { it.copy(creandoPremio = false, editandoPremio = null) }

    fun onGuardarPremio(draft: WheelPrizeDraft, existente: WheelPrize?) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (existente == null) {
                repository.createPrize(draft)
            } else {
                repository.updatePrize(
                    existente.copy(
                        rotulo = draft.rotulo,
                        detalle = draft.detalle,
                        peso = draft.peso,
                        otorgaPremio = draft.otorgaPremio,
                        orden = draft.orden,
                    )
                )
            }
            when (result) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(isSaving = false, creandoPremio = false, editandoPremio = null)
                    }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    fun onActivarPremio(prize: WheelPrize, activo: Boolean) {
        viewModelScope.launch {
            repository.updatePrize(prize.copy(activo = activo))
            refresh()
        }
    }

    // ---------- Códigos ----------

    fun onToggleSoloPendientes(solo: Boolean) {
        _uiState.update { it.copy(soloPendientes = solo) }
        refresh()
    }

    fun onBusqueda(v: String) = _uiState.update {
        it.copy(busqueda = v, codigoBuscado = null, busquedaSinResultado = false)
    }

    fun onVerificarCodigo() {
        val codigo = _uiState.value.busqueda.trim()
        if (codigo.isBlank()) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = repository.buscarCodigo(codigo)) {
                is DataResult.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        codigoBuscado = r.data,
                        busquedaSinResultado = r.data == null,
                    )
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    fun onCanjear(spin: WheelSpin, canjeado: Boolean) {
        viewModelScope.launch {
            when (val r = repository.marcarCanjeado(spin.id, canjeado)) {
                is DataResult.Success -> {
                    // Refrescar también la tarjeta de verificación si es la misma
                    _uiState.update { state ->
                        state.copy(
                            codigoBuscado = state.codigoBuscado?.takeIf { it.id == spin.id }
                                ?.copy(canjeado = canjeado)
                                ?: state.codigoBuscado,
                        )
                    }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(errorMessage = r.error.toMessage())
                }
            }
        }
    }

    // ---------- Bases ----------

    fun onEditarTermino(term: WheelTerm) = _uiState.update { it.copy(editandoTermino = term) }
    fun onCerrarTermino() = _uiState.update { it.copy(editandoTermino = null) }

    fun onGuardarTermino(term: WheelTerm) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val r = repository.updateTerm(term)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, editandoTermino = null) }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }
}
