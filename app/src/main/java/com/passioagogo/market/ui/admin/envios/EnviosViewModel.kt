package com.passioagogo.market.ui.admin.envios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.core.crypto.AdminKeyStore
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.data.addresses.AddressRepository
import com.passioagogo.market.data.addresses.DireccionClara
import com.passioagogo.market.data.addresses.jsonDireccion
import com.passioagogo.market.data.envio.DireccionEnClaro
import com.passioagogo.market.data.envio.EnvioApi
import com.passioagogo.market.data.envio.EnvioRepository
import com.passioagogo.market.data.envio.EnvioSolicitud
import com.passioagogo.market.data.envio.Guia
import com.passioagogo.market.data.envio.GuiaPendiente
import com.passioagogo.market.data.envio.Recoleccion
import com.passioagogo.market.data.envio.ResultadoGuia
import com.passioagogo.market.data.envio.ResultadoRecoleccion
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

data class EnviosUiState(
    val pendientes: List<EnvioSolicitud> = emptyList(),
    val conGuia: List<EnvioSolicitud> = emptyList(),
    val porRecoger: List<GuiaPendiente> = emptyList(),
    val claveDesbloqueada: Boolean = false,
    /** true si el archivo de la clave ya está en este dispositivo. */
    val tieneArchivoClave: Boolean = false,
    /** Diálogo de desbloqueo abierto. */
    val pidiendoClave: Boolean = false,
    val errorClave: String? = null,
    val abriendoClave: Boolean = false,
    val isLoading: Boolean = false,
    /** Solicitud cuya guía se está generando. */
    val generando: String? = null,
    val pidiendoRecoleccion: Boolean = false,
    /** Guía recién creada, para mostrar e imprimir. */
    val guiaGenerada: Guia? = null,
    val recoleccionHecha: Recoleccion? = null,
    val aviso: String? = null,
    val errorMessage: String? = null,
    /** Solicitud marcada para recolección antes de generar. */
    val marcadasParaRecoger: Set<String> = emptySet(),
    /** Solicitud para la que se está eligiendo otra paquetería. */
    val cambiandoPaqueteria: EnvioSolicitud? = null,
) {
    /** Las que esperan camión, agrupadas: una recolección por paquetería. */
    val porPaqueteria: List<Pair<String, List<GuiaPendiente>>>
        get() = porRecoger.groupBy { it.paqueteria }.toList().sortedBy { it.first }
}

@HiltViewModel
class EnviosViewModel @Inject constructor(
    private val envioRepository: EnvioRepository,
    private val envioApi: EnvioApi,
    private val addressRepository: AddressRepository,
    private val adminKeyStore: AdminKeyStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnviosUiState())
    val uiState: StateFlow<EnviosUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                claveDesbloqueada = adminKeyStore.desbloqueada.value,
                tieneArchivoClave = adminKeyStore.tieneArchivo(),
            )
        }
        viewModelScope.launch {
            val pendientes = envioRepository.pendientesDeGuia()
            val conGuia = envioRepository.conGuia()
            val porRecoger = envioApi.pendientesDeRecoleccion()

            _uiState.update { estado ->
                estado.copy(
                    isLoading = false,
                    pendientes = (pendientes as? DataResult.Success)?.data ?: estado.pendientes,
                    conGuia = (conGuia as? DataResult.Success)?.data ?: estado.conGuia,
                    porRecoger = porRecoger,
                    errorMessage = (pendientes as? DataResult.Error)?.error?.toMessage(),
                )
            }
        }
    }

    // ---- Clave de administración ----
    //
    // Se puede abrir desde aquí además de desde Clientes: sin ella no hay
    // dirección que leer, y mandar al usuario a otra sección para volver
    // es fricción sin motivo.

    fun onAbrirClave() = _uiState.update {
        it.copy(
            pidiendoClave = true,
            tieneArchivoClave = adminKeyStore.tieneArchivo(),
            errorClave = null,
        )
    }

    fun onCerrarClave() = _uiState.update {
        it.copy(pidiendoClave = false, errorClave = null)
    }

    fun onDesbloquear(contrasena: String) {
        _uiState.update { it.copy(abriendoClave = true, errorClave = null) }
        aplicarClave(adminKeyStore.desbloquear(contrasena))
    }

    fun onImportarClave(contenidoJson: String, contrasena: String) {
        _uiState.update { it.copy(abriendoClave = true, errorClave = null) }
        aplicarClave(adminKeyStore.importar(contenidoJson, contrasena))
    }

    private fun aplicarClave(resultado: Result<Unit>) {
        if (resultado.isSuccess) {
            _uiState.update {
                it.copy(
                    pidiendoClave = false,
                    claveDesbloqueada = true,
                    tieneArchivoClave = true,
                    abriendoClave = false,
                    errorClave = null,
                )
            }
        } else {
            // Mensaje único: distinguir «contraseña mala» de «archivo
            // corrupto» ayudaría a quien prueba contraseñas.
            _uiState.update {
                it.copy(
                    abriendoClave = false,
                    errorClave = "No se pudo abrir con esos datos. Revisa la " +
                        "contraseña y el archivo.",
                )
            }
        }
    }

    fun onMarcarRecoleccion(solicitudId: String, marcar: Boolean) = _uiState.update {
        it.copy(
            marcadasParaRecoger = if (marcar) it.marcadasParaRecoger + solicitudId
            else it.marcadasParaRecoger - solicitudId,
        )
    }

    fun onCerrarGuia() = _uiState.update { it.copy(guiaGenerada = null, aviso = null) }
    fun onCerrarRecoleccion() =
        _uiState.update { it.copy(recoleccionHecha = null, aviso = null) }
    fun onCerrarError() = _uiState.update { it.copy(errorMessage = null) }

    /**
     * Genera la guía de una solicitud.
     *
     * Abre la dirección cifrada del cliente aquí, con la clave privada, y
     * se la pasa a la Edge Function en claro y en memoria. Nunca se
     * guarda descifrada ni se escribe en disco: lo que está cifrado en la
     * base sigue cifrado.
     */
    fun onPedirCambioPaqueteria(solicitud: EnvioSolicitud) =
        _uiState.update { it.copy(cambiandoPaqueteria = solicitud) }

    fun onCancelarCambio() = _uiState.update { it.copy(cambiandoPaqueteria = null) }

    /**
     * Genera la guía con otra paquetería.
     *
     * Quien eligió una muchas veces no eligió un precio sino en quién
     * confía, así que esto pasa por una confirmación aparte y conviene
     * avisar al cliente después.
     */
    fun onGenerarConOtraPaqueteria(solicitud: EnvioSolicitud, carrier: String) {
        _uiState.update { it.copy(cambiandoPaqueteria = null) }
        onGenerarGuia(solicitud, carrier = carrier.trim().lowercase(), permitirCambio = true)
    }

    fun onGenerarGuia(
        solicitud: EnvioSolicitud,
        carrier: String? = null,
        permitirCambio: Boolean = false,
    ) {
        // Una guía a la vez: cada una cuesta dinero, y dos peticiones
        // simultáneas podrían emitir dos etiquetas pagadas.
        if (_uiState.value.generando != null) return

        if (!adminKeyStore.desbloqueada.value) {
            _uiState.update {
                it.copy(
                    errorMessage = "Primero abre la clave de administración en " +
                        "la sección Clientes: sin ella no se puede leer la dirección.",
                )
            }
            return
        }
        _uiState.update { it.copy(generando = solicitud.id, errorMessage = null) }
        viewModelScope.launch {
            // La dirección cifrada cuelga de quien levantó la solicitud.
            // Si la levantó un promotor, el sobre sería el del promotor,
            // no el del destinatario: ese caso se detecta abajo porque no
            // hay sobre, o porque la dirección no corresponde.
            val direccion = abrirDireccion(solicitud.createdBy)
            if (direccion == null) {
                _uiState.update {
                    it.copy(
                        generando = null,
                        errorMessage = "No hay dirección cifrada para esta solicitud. " +
                            "Si la levantó un promotor, el cliente no capturó domicilio " +
                            "en el sitio: pídeselo y regístralo antes de generar la guía.",
                    )
                }
                return@launch
            }

            val resultado = envioApi.generarGuia(
                solicitudId = solicitud.id,
                direccion = direccion,
                carrier = carrier,
                recoleccion = solicitud.id in _uiState.value.marcadasParaRecoger,
                permitirCambio = permitirCambio,
            )
            Log.i("EnviosViewModel", "resultado de solicitud ${resultado}")
            when (resultado) {
                is ResultadoGuia.Ok -> {
                    _uiState.update {
                        it.copy(
                            generando = null,
                            guiaGenerada = resultado.guia,
                            aviso = resultado.aviso,
                            marcadasParaRecoger = it.marcadasParaRecoger - solicitud.id,
                        )
                    }
                    refresh()
                }
                is ResultadoGuia.Fallo -> _uiState.update {
                    it.copy(generando = null, errorMessage = resultado.mensaje)
                }
            }
        }
    }

    /** Pide el camión para todas las guías de una paquetería. */
    fun onSolicitarRecoleccion(paqueteria: String, fecha: String) {
        _uiState.update { it.copy(pidiendoRecoleccion = true, errorMessage = null) }
        viewModelScope.launch {
            val resultado = envioApi.solicitarRecoleccion(
                fecha = fecha,
                paqueteria = paqueteria,
            )
            when (resultado) {
                is ResultadoRecoleccion.Ok -> {
                    _uiState.update {
                        it.copy(
                            pidiendoRecoleccion = false,
                            recoleccionHecha = resultado.recoleccion,
                            aviso = resultado.aviso,
                        )
                    }
                    refresh()
                }
                is ResultadoRecoleccion.Fallo -> _uiState.update {
                    it.copy(pidiendoRecoleccion = false, errorMessage = resultado.mensaje)
                }
            }
        }
    }

    /**
     * Saca la dirección del cliente del sobre cifrado.
     *
     * Toma la más reciente si tiene varias: es la que capturó al pedir.
     */
    private suspend fun abrirDireccion(userId: String): DireccionEnClaro? {
        val sobres = addressRepository.getDirecciones(userId)
        val sobre = (sobres as? DataResult.Success)?.data?.firstOrNull() ?: return null
        val claro = adminKeyStore.descifrar(sobre.contenido, sobre.iv, sobre.dkAdmin)
            ?: return null
        val datos = runCatching {
            jsonDireccion.decodeFromString<DireccionClara>(claro)
        }.getOrNull() ?: return null

        return DireccionEnClaro(
            calle = datos.calle,
            interior = datos.interior,
            colonia = datos.colonia,
            cp = datos.cp,
            ciudad = datos.ciudad,
            estado = datos.estado,
            // El sobre no guarda el código de dos letras: el servidor lo
            // deduce del código postal cuando falta.
            estadoCodigo = "",
            referencias = datos.referencias,
        )
    }
}

/** Mañana en formato AAAA-MM-DD, que es lo que espera la recolección. */
internal fun fechaManana(): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}
