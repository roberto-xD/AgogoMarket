package com.passioagogo.market.ui.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.passioagogo.market.core.pdf.PdfLocal
import java.io.File

/**
 * Muestra un PDF dentro de la app.
 *
 * Evita salir a otra aplicación para ver una guía, que en un teléfono de
 * mostrador suele significar perder el sitio donde estabas.
 *
 * Pellizca para acercar, doble toque para alternar, y el botón de
 * compartir manda el archivo tal cual, no una captura.
 */
@Composable
fun VisorPdf(
    url: String,
    titulo: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val configuracion = LocalConfiguration.current
    val densidad = LocalDensity.current

    var paginas by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var archivo by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }

    // Se rasteriza al doble del ancho de pantalla: el zoom amplía el
    // bitmap ya generado, así que con holgura el texto no se pixela.
    val anchoPx = remember(configuracion) {
        with(densidad) { configuracion.screenWidthDp.dp.toPx() }.toInt() * 2
    }

    LaunchedEffect(url) {
        cargando = true
        error = null
        val descarga = PdfLocal.descargar(context, url)
        descarga.onSuccess { f ->
            archivo = f
            PdfLocal.paginas(f, anchoPx)
                .onSuccess { paginas = it }
                .onFailure { error = "No se pudo leer el PDF." }
        }.onFailure {
            error = "No se pudo descargar la guía. Revisa la conexión."
        }
        cargando = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF202020)),
        ) {
            when {
                cargando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }

                error != null -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    Alignment.Center,
                ) {
                    Text(
                        error.orEmpty(),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> PaginasConZoom(paginas)
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
            }

            Text(
                titulo,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
            )

            archivo?.let { f ->
                IconButton(
                    onClick = { compartirPdf(context, f, titulo) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Compartir guía",
                        tint = Color.White,
                    )
                }
            }

            if (paginas.size > 1) {
                Text(
                    "${paginas.size} páginas",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PaginasConZoom(paginas: List<Bitmap>) {
    var escala by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transform = rememberTransformableState { cambioEscala, pan, _ ->
        escala = (escala * cambioEscala).coerceIn(1f, 6f)
        if (escala > 1f) {
            offsetX += pan.x
            offsetY += pan.y
        } else {
            // Al volver al tamaño original se recentra, si no la página
            // queda desplazada fuera de la vista.
            offsetX = 0f
            offsetY = 0f
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .transformable(transform)
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
            }
            .graphicsLayer(
                scaleX = escala,
                scaleY = escala,
                translationX = offsetX,
                translationY = offsetY,
            ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 56.dp,
            bottom = 56.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(paginas) { _, pagina ->
            Image(
                bitmap = pagina.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            )
        }
    }
}

/**
 * Comparte el PDF tal cual, no una captura: quien lo reciba necesita la
 * guía imprimible, no una imagen de ella.
 */
private fun compartirPdf(context: Context, archivo: File, titulo: String) {
    runCatching {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir guía"))
    }
}
