package com.passioagogo.market.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Descarga un PDF y lo convierte en páginas para mostrarlas en la app.
 *
 * Se usa `PdfRenderer` del propio Android en vez de una librería externa:
 * no añade peso y es suficiente para una etiqueta de envío, que son una
 * o dos páginas.
 *
 * `PdfRenderer` exige un archivo local navegable, así que el PDF se baja
 * primero al caché. De paso queda disponible para compartirlo sin volver
 * a descargarlo.
 */
object PdfLocal {

    /** PdfRenderer no admite accesos concurrentes sobre el mismo documento. */
    private val candado = Mutex()

    private const val CARPETA = "compartir"

    /**
     * Baja el PDF al caché. Si ya estaba, lo reutiliza.
     *
     * El nombre sale de la URL, así que abrir dos veces la misma guía no
     * la descarga dos veces.
     */
    suspend fun descargar(context: Context, url: String): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val carpeta = File(context.cacheDir, CARPETA).apply { mkdirs() }
                val destino = File(carpeta, "guia_${url.hashCode()}.pdf")
                if (destino.exists() && destino.length() > 0) return@runCatching destino

                val conexion = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 20_000
                    instanceFollowRedirects = true
                }
                conexion.inputStream.use { entrada ->
                    destino.outputStream().use { salida -> entrada.copyTo(salida) }
                }
                if (destino.length() == 0L) error("El archivo llegó vacío")
                destino
            }
        }

    /**
     * Convierte el PDF en bitmaps, uno por página.
     *
     * [anchoPx] es el ancho al que se rasteriza: se pasa el de la pantalla
     * para que el texto se lea nítido. El zoom amplía el bitmap ya
     * generado, así que conviene rasterizar con holgura.
     */
    suspend fun paginas(archivo: File, anchoPx: Int): Result<List<Bitmap>> =
        withContext(Dispatchers.IO) {
            candado.withLock {
                runCatching {
                    ParcelFileDescriptor.open(
                        archivo,
                        ParcelFileDescriptor.MODE_READ_ONLY,
                    ).use { descriptor ->
                        PdfRenderer(descriptor).use { renderer ->
                            (0 until renderer.pageCount).map { indice ->
                                renderer.openPage(indice).use { pagina ->
                                    val escala = anchoPx.toFloat() / pagina.width
                                    val alto = (pagina.height * escala).toInt()
                                        .coerceAtLeast(1)
                                    val bitmap = Bitmap.createBitmap(
                                        anchoPx.coerceAtLeast(1),
                                        alto,
                                        Bitmap.Config.ARGB_8888,
                                    )
                                    // Fondo blanco: el PDF no lo trae y sin
                                    // esto las zonas vacías salen negras.
                                    bitmap.eraseColor(Color.WHITE)
                                    pagina.render(
                                        bitmap,
                                        null,
                                        null,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                                    )
                                    bitmap
                                }
                            }
                        }
                    }
                }
            }
        }
}
