package com.passioagogo.market.core.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.PrivateKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Custodia la clave privada de administración mientras dura la sesión.
 *
 * Singleton porque la clave se desbloquea una vez y se usa desde varias
 * pantallas; un ViewModel por pantalla obligaría a teclear la contraseña
 * maestra cada vez.
 *
 * **La clave vive solo en memoria.** Lo que sí se guarda en disco es el
 * archivo `admin-privada.json`, que ya viene cifrado con la contraseña
 * maestra: sin ella no sirve de nada. Eso evita tener que elegir el
 * archivo en cada arranque sin rebajar la protección, porque el secreto
 * real sigue siendo la contraseña y esa nunca se persiste.
 */
@Singleton
class AdminKeyStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        const val ARCHIVO = "admin-privada.json"
    }

    private val _desbloqueada = MutableStateFlow(false)
    val desbloqueada: StateFlow<Boolean> = _desbloqueada.asStateFlow()

    private var privada: PrivateKey? = null

    private val archivo: File get() = File(context.filesDir, ARCHIVO)

    /** true si ya se importó el archivo en este dispositivo. */
    fun tieneArchivo(): Boolean = archivo.exists()

    /**
     * Guarda el archivo cifrado en el almacenamiento privado de la app.
     * Valida que sea legible antes de conservarlo, para no descubrir que
     * estaba corrupto cuando ya haya direcciones que abrir.
     */
    fun importar(contenidoJson: String, contrasenaMaestra: String): Result<Unit> =
        runCatching {
            // Si esto lanza, el archivo o la contraseña no sirven: no se guarda
            val clave = AdminKeyTool.abrirPrivada(contenidoJson, contrasenaMaestra)
            archivo.writeText(contenidoJson)
            privada = clave
            _desbloqueada.value = true
        }

    /**
     * Desbloquea con la contraseña maestra.
     *
     * No distingue «contraseña incorrecta» de «archivo corrupto»: decirlo
     * le confirmaría a quien prueba contraseñas que va por buen camino.
     */
    fun desbloquear(contrasenaMaestra: String): Result<Unit> = runCatching {
        require(archivo.exists()) { "Primero importa el archivo de la clave" }
        privada = AdminKeyTool.abrirPrivada(archivo.readText(), contrasenaMaestra)
        _desbloqueada.value = true
    }

    /**
     * Genera un par nuevo y deja el archivo cifrado en el dispositivo.
     *
     * Devuelve la pública (para registrarla en `crypto_keys`) y el
     * contenido del archivo, que el administrador DEBE exportar y guardar
     * fuera del teléfono: si desinstala la app sin copia, las direcciones
     * ya guardadas quedan ilegibles para siempre.
     */
    fun generar(contrasenaMaestra: String, id: String): Result<AdminKeyTool.ParGenerado> =
        runCatching {
            val par = AdminKeyTool.generarPar(contrasenaMaestra, id)
            archivo.writeText(par.archivoPrivada)
            privada = AdminKeyTool.abrirPrivada(par.archivoPrivada, contrasenaMaestra)
            _desbloqueada.value = true
            par
        }

    /** Contenido del archivo guardado, para exportarlo como respaldo. */
    fun contenidoArchivo(): String? = archivo.takeIf { it.exists() }?.readText()

    /** Suelta la clave. Se llama al cerrar sesión. */
    fun bloquear() {
        privada = null
        _desbloqueada.value = false
    }

    /** Borra el archivo del dispositivo; habrá que volver a importarlo. */
    fun olvidarArchivo() {
        bloquear()
        archivo.delete()
    }

    /**
     * Descifra una dirección. Devuelve null si la clave no está
     * desbloqueada o si esa dirección se envolvió con otra clave
     * (por ejemplo, tras una rotación).
     */
    fun descifrar(contenidoB64: String, ivB64: String, dkAdminB64: String): String? {
        val clave = privada ?: return null
        return runCatching {
            AdminKeyTool.descifrarDireccion(contenidoB64, ivB64, dkAdminB64, clave)
        }.getOrNull()
    }
}
