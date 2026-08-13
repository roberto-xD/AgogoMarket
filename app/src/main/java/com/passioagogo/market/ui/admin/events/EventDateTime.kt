package com.passioagogo.market.ui.admin.events

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Conversión entre el ISO-8601 UTC que guarda PostgreSQL y los campos
 * separados de fecha y hora que edita la UI, en hora local.
 *
 * Se trabaja en hora local a propósito: quien captura un evento piensa
 * en "el 14 a las 19:00", no en UTC.
 */
object EventDateTime {

    private val isoUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = true
    }
    private val diaLocal = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val horaLocal = SimpleDateFormat("HH:mm", Locale.US)
    private val presentacion = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "MX"))

    /** ISO del servidor → Date, tolerando fracciones de segundo y offsets. */
    private fun parse(iso: String): Date? = runCatching {
        val limpio = iso.substringBefore('.').substringBefore('+').removeSuffix("Z")
        isoParser.parse(limpio)
    }.getOrNull()

    fun toDiaLocal(iso: String?): String =
        iso?.let { parse(it) }?.let { diaLocal.format(it) } ?: ""

    fun toHoraLocal(iso: String?): String =
        iso?.let { parse(it) }?.let { horaLocal.format(it) } ?: ""

    fun paraMostrar(iso: String?): String =
        iso?.let { parse(it) }?.let { presentacion.format(it) } ?: "—"

    /** Milisegundos UTC del día seleccionado en el DatePicker → "yyyy-MM-dd" local. */
    fun diaDesdeMillis(millis: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = millis
        }
        return "%04d-%02d-%02d".format(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH) + 1,
            utc.get(Calendar.DAY_OF_MONTH),
        )
    }

    /** "yyyy-MM-dd" + "HH:mm" en hora local → ISO UTC para el servidor. */
    fun aIsoUtc(dia: String, hora: String): String? {
        if (dia.isBlank()) return null
        val partesDia = dia.split("-").mapNotNull { it.toIntOrNull() }
        if (partesDia.size != 3) return null
        val partesHora = hora.split(":").mapNotNull { it.toIntOrNull() }
        val h = partesHora.getOrElse(0) { 0 }
        val m = partesHora.getOrElse(1) { 0 }

        val local = Calendar.getInstance().apply {
            clear()
            set(partesDia[0], partesDia[1] - 1, partesDia[2], h, m, 0)
        }
        return isoUtc.format(local.time)
    }

    fun esFutura(iso: String?): Boolean =
        iso?.let { parse(it) }?.after(Date()) ?: false
}
