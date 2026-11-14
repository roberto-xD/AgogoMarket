package com.passioagogo.market.ui.admin.codigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.codigos.Codigo
import com.passioagogo.market.data.codigos.CodigoDraft
import com.passioagogo.market.data.codigos.CodigoRepository
import com.passioagogo.market.data.codigos.TipoCodigo
import com.passioagogo.market.ui.common.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que se está capturando en el diálogo de alta. */
data class FormularioCodigo(
    val codigo: String = "",
    val tipo: TipoCodigo = TipoCodigo.DESCUENTO,
    val descuentoPct: String = "15",
    val regalo: String = "",
    val minimoCompra: String = "",
    val usosMax: String = "1",
    /** Días de vigencia; vacío = sin caducidad. */
    val diasVigencia: String = "30",
    val nota: String = "",
) {
    /** El CHECK de la tabla: mayúsculas y de 4 a 24 caracteres. */
    val codigoValido: Boolean
        get() = codigo.trim().length in 4..24

    val usosValidos: Boolean get() = (usosMax.toIntOrNull() ?: 0) >= 1

    /**
     * Coherencia por tipo, la misma que exige `codigos_tipo_coherente`:
     * un descuento sin porcentaje no descuenta, y un regalo sin detalle
     * deja a quien prepara la caja sin saber qué meter.
     */
    val datosDelTipoValidos: Boolean
        get() = when (tipo) {
            TipoCodigo.DESCUENTO -> (descuentoPct.toIntOrNull() ?: 0) in 1..100
            TipoCodigo.REGALO -> regalo.isNotBlank()
            TipoCodigo.ENVIO_GRATIS -> minimoCompra.isBlank() ||
                (minimoCompra.toDoubleOrNull() ?: -1.0) >= 0
        }

    val valido: Boolean get() = codigoValido && usosValidos && datosDelTipoValidos
}

data class CodigosUiState(
    val codigos: List<Codigo> = emptyList(),
    val soloManuales: Boolean = true,
    val soloCanjeables: Boolean = false,
    val isLoading: Boolean = false,
    val creando: Boolean = false,
    val formulario: FormularioCodigo = FormularioCodigo(),
    val isSaving: Boolean = false,
    /** Código que se está editando; null = ninguno. */
    val editando: Codigo? = null,
    val edicion: EdicionCodigo = EdicionCodigo(),
    /** Confirmación del borrado lógico. */
    val porOcultar: Codigo? = null,
    /** Cuál tiene el panel abierto: solo una a la vez. */
    val deslizada: String? = null,
    val errorMessage: String? = null,
) {
    /** Instante actual en ISO, para decidir qué está caducado. */
    val ahoraIso: String
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())

    val visibles: List<Codigo>
        get() = if (soloCanjeables) codigos.filter { it.canjeable(ahoraIso) }
        else codigos

    val canjeables: Int get() = codigos.count { it.canjeable(ahoraIso) }
}

/**
 * Lo que se puede cambiar de un código ya repartido.
 *
 * El tipo y el porcentaje no están: quien recibió «15%» tiene que seguir
 * recibiendo 15%. Cambiar eso sería reescribir lo prometido.
 */
data class EdicionCodigo(
    val diasVigencia: String = "",
    val usosMax: String = "1",
    val nota: String = "",
) {
    val usosValidos: Boolean get() = (usosMax.toIntOrNull() ?: 0) >= 1
    val valido: Boolean get() = usosValidos
}

@HiltViewModel
class CodigosViewModel @Inject constructor(
    private val repository: CodigoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodigosUiState())
    val uiState: StateFlow<CodigosUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun onSoloManuales(v: Boolean) {
        _uiState.update { it.copy(soloManuales = v) }
        refresh()
    }

    fun onSoloCanjeables(v: Boolean) = _uiState.update { it.copy(soloCanjeables = v) }

    fun onDeslizada(id: String?) = _uiState.update { it.copy(deslizada = id) }

    // ---- Editar ----

    fun onEditar(codigo: Codigo) = _uiState.update {
        it.copy(
            editando = codigo,
            edicion = EdicionCodigo(
                // Vacío = sin caducidad; si la tiene, se recaptura en días
                diasVigencia = "",
                usosMax = codigo.usosMax.toString(),
                nota = codigo.nota.orEmpty(),
            ),
        )
    }

    fun onCerrarEdicion() = _uiState.update { it.copy(editando = null) }
    fun onEdicion(e: EdicionCodigo) = _uiState.update { it.copy(edicion = e) }

    fun onGuardarEdicion() {
        val codigo = _uiState.value.editando ?: return
        val e = _uiState.value.edicion
        if (!e.valido) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val nuevosUsos = e.usosMax.toIntOrNull() ?: codigo.usosMax
            val r = repository.editar(
                id = codigo.id,
                // Vacío conserva lo que tenía: así no se borra la
                // caducidad por no volver a teclearla.
                expiraAt = e.diasVigencia.toIntOrNull()?.let { caducidad(it) }
                    ?: codigo.expiraAt,
                usosMax = nuevosUsos,
                nota = e.nota,
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    editando = null,
                    errorMessage = (r as? DataResult.Error)?.error?.toMessage(),
                )
            }
            refresh()
        }
    }

    // ---- Ocultar ----

    fun onPedirOcultar(codigo: Codigo) = _uiState.update { it.copy(porOcultar = codigo) }
    fun onCancelarOcultar() = _uiState.update { it.copy(porOcultar = null) }

    fun onOcultar() {
        val codigo = _uiState.value.porOcultar ?: return
        _uiState.update { it.copy(porOcultar = null) }
        viewModelScope.launch {
            repository.ocultar(codigo.id, true)
            refresh()
        }
    }

    fun onAbrirAlta() = _uiState.update {
        it.copy(creando = true, formulario = FormularioCodigo(), errorMessage = null)
    }

    fun onCerrarAlta() = _uiState.update { it.copy(creando = false) }
    fun onCerrarError() = _uiState.update { it.copy(errorMessage = null) }

    fun onFormulario(f: FormularioCodigo) = _uiState.update { it.copy(formulario = f) }

    fun onCrear() {
        val f = _uiState.value.formulario
        if (!f.valido) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val draft = CodigoDraft(
                codigo = f.codigo,
                tipo = f.tipo,
                // Solo el dato que corresponde al tipo: los demás van
                // nulos o el CHECK de coherencia rechaza la fila.
                descuentoPct = if (f.tipo == TipoCodigo.DESCUENTO) {
                    f.descuentoPct.toIntOrNull()
                } else null,
                regalo = if (f.tipo == TipoCodigo.REGALO) f.regalo else null,
                minimoCompra = if (f.tipo == TipoCodigo.ENVIO_GRATIS) {
                    f.minimoCompra.toDoubleOrNull()
                } else null,
                usosMax = f.usosMax.toIntOrNull() ?: 1,
                expiraAt = caducidad(f.diasVigencia.toIntOrNull()),
                nota = f.nota,
            )
            when (val r = repository.crear(draft)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, creando = false) }
                    refresh()
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = r.error.toMessage())
                }
            }
        }
    }

    /** Cortar una campaña a media marcha, sin borrarla. */
    fun onCambiarActivo(codigo: Codigo) {
        viewModelScope.launch {
            repository.cambiarActivo(codigo.id, !codigo.activo)
            refresh()
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val r = repository.getCodigos(soloManuales = _uiState.value.soloManuales)
            _uiState.update { estado ->
                estado.copy(
                    isLoading = false,
                    codigos = (r as? DataResult.Success)?.data ?: estado.codigos,
                    errorMessage = (r as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    /** Fecha de caducidad en ISO; null = sin caducidad. */
    private fun caducidad(dias: Int?): String? {
        if (dias == null || dias <= 0) return null
        val fin = Date(System.currentTimeMillis() + dias * 24L * 60 * 60 * 1000)
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(fin)
    }
}
