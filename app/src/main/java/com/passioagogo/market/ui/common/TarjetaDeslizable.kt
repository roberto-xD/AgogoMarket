package com.passioagogo.market.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Una acción del panel que queda al descubierto al deslizar. */
data class AccionDeslizable(
    val etiqueta: String,
    val icono: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
)

/**
 * Tarjeta que descubre acciones al deslizarla hacia la izquierda.
 *
 * A diferencia de `SwipeToDismissBox`, el panel **se queda abierto**: el
 * gesto solo lo revela y la acción la decide un toque. Eso evita que un
 * deslizamiento a medias dispare un borrado, que es el riesgo de usar el
 * swipe para algo que cuesta deshacer.
 *
 * Se cierra al tocar la tarjeta, al ejecutar una acción, y cuando
 * [cerrar] cambia de valor — pasando ahí el id de la tarjeta abierta se
 * consigue que solo haya una abierta a la vez.
 *
 * Genérica a propósito: recibe las acciones y el contenido, así que
 * sirve para cualquier lista.
 */
@Composable
fun TarjetaDeslizable(
    acciones: List<AccionDeslizable>,
    modifier: Modifier = Modifier,
    anchoAccion: Int = 88,
    cerrar: Any? = null,
    onClick: (() -> Unit)? = null,
    contenido: @Composable () -> Unit,
) {
    val densidad = LocalDensity.current
    val anchoPanelPx = with(densidad) { (anchoAccion * acciones.size).dp.toPx() }

    var desplazamiento by remember { mutableFloatStateOf(0f) }
    val animado by animateFloatAsState(desplazamiento, label = "deslizar")

    // Al abrirse otra tarjeta, esta se recoge
    LaunchedEffect(cerrar) { desplazamiento = 0f }

    val arrastre = rememberDraggableState { delta ->
        // Solo hacia la izquierda, y nunca más allá del panel
        desplazamiento = (desplazamiento + delta).coerceIn(-anchoPanelPx, 0f)
    }

    Box(modifier.fillMaxWidth()) {
        // Las acciones viven detrás, pegadas a la derecha
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
        ) {
            acciones.forEach { accion ->
                Column(
                    modifier = Modifier
                        .width(anchoAccion.dp)
                        .fillMaxHeight()
                        .background(accion.color)
                        .clickable {
                            // Se recoge antes de actuar: si la acción abre
                            // un diálogo, la tarjeta no se queda abierta
                            // debajo esperando.
                            desplazamiento = 0f
                            accion.onClick()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        accion.icono,
                        contentDescription = accion.etiqueta,
                        tint = Color.White,
                    )
                    Text(
                        accion.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }

        Box(
            Modifier
                .offset { IntOffset(animado.roundToInt(), 0) }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .draggable(
                    state = arrastre,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        // Más de la mitad descubierto se queda abierto;
                        // menos, se recoge. Sin esto queda a medias.
                        desplazamiento = if (desplazamiento < -anchoPanelPx / 2) {
                            -anchoPanelPx
                        } else 0f
                    },
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable {
                            // Con el panel abierto, el toque lo cierra en vez
                            // de entrar: es lo que se espera al tocar fuera
                            // de un menú.
                            if (desplazamiento != 0f) desplazamiento = 0f else onClick()
                        }
                    } else Modifier
                ),
        ) { contenido() }
    }
}
