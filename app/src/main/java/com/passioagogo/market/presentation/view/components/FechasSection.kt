package com.passioagogo.market.presentation.view.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FechaActualSection(
    fechaCreacion: Long,
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row {
                Text(
                    text = "Venta",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormatter.format(Date(fechaCreacion)),
                    modifier = Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Right,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FechaLiquidacionSection(
    fechaLiquidacion: Long?,
    onFechaLiquidacionChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Fecha Liquidación (seleccionable)
    OutlinedTextField(
        value = fechaLiquidacion?.let { dateFormatter.format(Date(it)) } ?: "",
        onValueChange = {},
        label = { Text("Fecha de Liquidación (opcional)") },
        placeholder = { Text("Seleccionar fecha") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                mostrarDateTimePicker(context, fechaLiquidacion, onFechaLiquidacionChange)
            }) {
                Icon(Icons.Default.Info, "Seleccionar fecha")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (fechaLiquidacion != null) {
        TextButton(
            onClick = { onFechaLiquidacionChange(null) },
            modifier = Modifier //.align(Alignment.End)
        ) {
            Text("Limpiar fecha")
        }
    }
}

fun mostrarDateTimePicker(
    context: Context,
    fechaActual: Long?,
    onFechaSeleccionada: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    fechaActual?.let { calendar.timeInMillis = it }

    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute)
                    onFechaSeleccionada(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
@Preview
fun FechaActualSectionPreview() {
    FechaActualSection(fechaCreacion = System.currentTimeMillis())
}