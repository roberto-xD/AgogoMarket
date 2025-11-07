package com.passioagogo.market.data.mapper

import java.text.SimpleDateFormat
import java.util.*

object DateMapper {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val isoFormatShort = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parseIsoDate(isoDate: String?): Long? {
        if (isoDate == null) return null
        return try {
            // Intentar primero con formato largo
            isoFormat.parse(isoDate)?.time
        } catch (e: Exception) {
            try {
                // Intentar con formato corto
                isoFormatShort.parse(isoDate)?.time
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun formatToIso(timestamp: Long?): String? {
        if (timestamp == null) return null
        return try {
            isoFormat.format(Date(timestamp))
        } catch (e: Exception) {
            null
        }
    }

    fun formatToIsoNonNull(timestamp: Long): String {
        return isoFormat.format(Date(timestamp))
    }
}