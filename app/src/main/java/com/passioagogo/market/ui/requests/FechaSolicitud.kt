package com.passioagogo.market.ui.requests

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formatea el timestamptz que devuelve PostgREST a hora local.
 *
 * El servidor manda ISO-8601 UTC con fracciones de segundo variables
 * ("2026-08-20T18:09:36.123456+00:00"), así que se recorta antes de
 * interpretarlo en lugar de exigir un patrón exacto.
 */
internal object FechaSolicitud {

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = true
    }
    private val corta = SimpleDateFormat("d MMM, HH:mm", Locale("es", "MX"))
    private val larga = SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "MX"))

    private fun parse(iso: String?): Date? {
        if (iso.isNullOrBlank()) return null
        val limpio = iso.substringBefore('.').substringBefore('+').removeSuffix("Z")
        return runCatching { parser.parse(limpio) }.getOrNull()
    }

    fun corta(iso: String?): String = parse(iso)?.let { corta.format(it) } ?: ""

    fun larga(iso: String?): String = parse(iso)?.let { larga.format(it) } ?: "—"
}
