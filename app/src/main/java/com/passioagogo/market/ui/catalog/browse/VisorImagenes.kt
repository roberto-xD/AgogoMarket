package com.passioagogo.market.ui.catalog.browse

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Descarga la imagen al caché y la comparte como archivo.
 *
 * Se comparte el archivo y no la URL porque así el receptor recibe una
 * imagen de verdad (WhatsApp la muestra, la galería la guarda) en lugar
 * de un enlace que hay que abrir.
 *
 * Reutiliza el caché de Coil: si la imagen ya se vio, no vuelve a bajarla.
 */
suspend fun compartirImagen(
    context: Context,
    url: String,
    texto: String? = null,
): Result<Unit> = runCatching {
    val bitmap = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // los bitmaps de hardware no se pueden escribir
            .build()
        val resultado = context.imageLoader.execute(request)
        (resultado.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            ?: error("No se pudo obtener la imagen")
    }

    val archivo = withContext(Dispatchers.IO) {
        val carpeta = File(context.cacheDir, "compartir").apply { mkdirs() }
        // Nombre fijo por URL: evita llenar el caché al compartir lo mismo
        val destino = File(carpeta, "img_${url.hashCode()}.jpg")
        destino.outputStream().use { salida ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, salida)
        }
        destino
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo,
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        // WhatsApp y Telegram lo usan como pie de foto; el correo, como
        // cuerpo. Algunas apps (Instagram) lo ignoran: la imagen siempre
        // llega, el texto es un extra que depende del destino.
        texto?.takeIf { it.isNotBlank() }?.let {
            putExtra(Intent.EXTRA_TEXT, it)
            putExtra(Intent.EXTRA_SUBJECT, it.lineSequence().first())
        }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir imagen"))
}

/**
 * Visor a pantalla completa: desliza entre imágenes, pellizca para acercar
 * y comparte la que está en pantalla.
 */
@Composable
fun VisorImagenes(
    imagenes: List<String>,
    indiceInicial: Int,
    onDismiss: () -> Unit,
    /** Pie de foto al compartir: nombre del producto, precio, etc. */
    textoCompartir: String? = null,
) {
    if (imagenes.isEmpty()) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var compartiendo by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = indiceInicial.coerceIn(0, imagenes.lastIndex),
        pageCount = { imagenes.size },
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(state = pagerState) { pagina ->
                ImagenConZoom(url = imagenes[pagina])
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
            }

            IconButton(
                enabled = !compartiendo,
                onClick = {
                    compartiendo = true
                    scope.launch {
                        val resultado = compartirImagen(
                            context = context,
                            url = imagenes[pagerState.currentPage],
                            texto = textoCompartir,
                        )
                        compartiendo = false
                        if (resultado.isFailure) {
                            Toast.makeText(
                                context,
                                "No se pudo compartir la imagen",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                if (compartiendo) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(4.dp),
                    )
                } else {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = Color.White)
                }
            }

            if (imagenes.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imagenes.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ImagenConZoom(url: String) {
    var escala by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformState = rememberTransformableState { cambioEscala, cambioPan, _ ->
        escala = (escala * cambioEscala).coerceIn(1f, 5f)
        if (escala > 1f) {
            offsetX += cambioPan.x
            offsetY += cambioPan.y
        } else {
            // Al volver al tamaño original se recentra: si no, la imagen
            // queda desplazada fuera de la vista.
            offsetX = 0f
            offsetY = 0f
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .transformable(transformState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (escala > 1f) {
                            escala = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            escala = 2.5f
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = escala,
                    scaleY = escala,
                    translationX = offsetX,
                    translationY = offsetY,
                ),
        )
    }
}
