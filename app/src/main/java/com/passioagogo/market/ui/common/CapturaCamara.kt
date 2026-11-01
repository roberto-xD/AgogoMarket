package com.passioagogo.market.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Permite tomar una foto con la cámara y devolver su [Uri].
 *
 * `TakePicture` escribe en un destino que hay que darle de antemano, así
 * que se crea un archivo en el caché y se expone con el `FileProvider`
 * que ya usa el resto de la app. El archivo queda en caché: quien lo
 * consuma debe subirlo o copiarlo, no confiar en que siga ahí.
 *
 * El permiso de cámara se pide solo si no está concedido; declararlo en
 * el manifiesto no basta desde Android 6.
 */
@Composable
fun rememberCapturaCamara(
    onFoto: (Uri) -> Unit,
): CapturaCamara {
    val context = LocalContext.current
    var destino by remember { mutableStateOf<Uri?>(null) }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        // exito es false si la persona cancela: ahí no hay foto que usar
        if (exito) destino?.let(onFoto)
    }

    val permiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            destino = crearDestino(context)
            destino?.let { camara.launch(it) }
        }
    }

    return remember(camara, permiso) {
        CapturaCamara(
            context = context,
            camara = camara,
            permiso = permiso,
            fijarDestino = { destino = it },
        )
    }
}

/** Dispara la captura, pidiendo permiso si hace falta. */
class CapturaCamara internal constructor(
    private val context: Context,
    private val camara: ManagedActivityResultLauncher<Uri, Boolean>,
    private val permiso: ManagedActivityResultLauncher<String, Boolean>,
    private val fijarDestino: (Uri?) -> Unit,
) {
    fun tomarFoto() {
        val concedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (concedido) {
            val uri = crearDestino(context)
            fijarDestino(uri)
            uri?.let { camara.launch(it) }
        } else {
            permiso.launch(Manifest.permission.CAMERA)
        }
    }
}

/** Archivo temporal en el caché, expuesto con el FileProvider de la app. */
private fun crearDestino(context: Context): Uri? = runCatching {
    val carpeta = File(context.cacheDir, "capturas").apply { mkdirs() }
    val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo,
    )
}.getOrNull()
