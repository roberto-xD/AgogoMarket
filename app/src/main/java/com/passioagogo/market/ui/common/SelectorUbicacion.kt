package com.passioagogo.market.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passioagogo.market.domain.inventory.Location

/**
 * Selector de ubicación que se encoge una vez elegida.
 *
 * Sin selección ocupa el ancho completo como campo de formulario: es la
 * decisión pendiente y conviene que destaque. Ya elegida se contrae a una
 * chip del ancho del nombre, porque durante el resto de la tarea casi
 * nunca se cambia y esa fila completa deja de ganarse su espacio.
 *
 * La transición entre ambas formas la anima `animateContentSize`.
 *
 * @param opcionTodas rótulo de la opción «sin filtrar»; null la omite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorUbicacion(
    locations: List<Location>,
    locationId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Ubicación",
    opcionTodas: String? = null,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val seleccionada = locations.firstOrNull { it.id == locationId }

    // «Todas» cuenta como elección hecha: también se contrae.
    val hayEleccion = seleccionada != null || (locationId == null && opcionTodas != null)

    Box(modifier.animateContentSize()) {
        if (hayEleccion && enabled) {
            AssistChip(
                onClick = { expanded = true },
                label = { Text(seleccionada?.nombre ?: opcionTodas.orEmpty()) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = etiqueta,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Cambiar",
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = AssistChipDefaults.assistChipColors(),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                opcionTodas?.let { todas ->
                    DropdownMenuItem(
                        text = { Text(todas) },
                        onClick = { expanded = false; onSelected(null) },
                    )
                }
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location.nombre) },
                        onClick = { expanded = false; onSelected(location.id) },
                    )
                }
            }
        } else {
            // Pendiente de elegir: forma de campo, a ancho completo.
            ExposedDropdownMenuBox(
                expanded = expanded && enabled,
                onExpandedChange = { if (enabled) expanded = it },
            ) {
                OutlinedTextField(
                    value = seleccionada?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = enabled,
                    label = { Text(etiqueta) },
                    placeholder = { Text("Elige una ubicación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    opcionTodas?.let { todas ->
                        DropdownMenuItem(
                            text = { Text(todas) },
                            onClick = { expanded = false; onSelected(null) },
                        )
                    }
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location.nombre) },
                            onClick = { expanded = false; onSelected(location.id) },
                        )
                    }
                }
            }
        }
    }
}
