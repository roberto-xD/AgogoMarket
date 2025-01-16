package com.passioagogo.market.ui.decorators

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned

import androidx.compose.ui.unit.IntSize

private val linealGradientColors = listOf(
    Color(0xFFDEDEDE),
    Color(0xFFC9C9C9),
    Color(0xFFDEDEDE),
)
fun Modifier.shimmerEffect(
    durationMillis: Int = 1000,
    gradientColors: List<Color> = linealGradientColors
): Modifier = composed {
    val size = remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition()
    val startOffsetX = transition.animateFloat(
        initialValue = -2 * size.value.width.toFloat(),
        targetValue = 2 * size.value.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                delayMillis = 250
            )
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = linealGradientColors,
            start = Offset(
                x = startOffsetX.value,
                y = 0f
            ),
            end = Offset(
                x = startOffsetX.value + size.value.width.toFloat(),
                y = size.value.height.toFloat()
            )
        )
    )
        .onGloballyPositioned {
            size.value = it.size
        }
}