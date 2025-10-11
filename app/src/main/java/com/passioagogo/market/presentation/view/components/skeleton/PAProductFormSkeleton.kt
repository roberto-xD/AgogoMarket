package com.passioagogo.market.presentation.view.components.skeleton

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProductFormSkeleton() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Título
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de información básica
        SkeletonCard(brush) {
            // Imagen del producto
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonInputField(brush)
                    Spacer(modifier = Modifier.height(16.dp))
                    SkeletonInputField(brush)
                    Spacer(modifier = Modifier.height(16.dp))
                    SkeletonInputField(brush)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información de ventas
        SkeletonCard(brush) {
            SkeletonInputField(brush)
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonInputField(brush, height = 100.dp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seguimiento del artículo
        SkeletonCard(brush) {
            SkeletonInputField(brush)
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonInputField(brush)
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonInputField(brush)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Atributos
        SkeletonCard(brush) {
            SkeletonInputField(brush)
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonInputField(brush)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Guardar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(brush)
        )
    }
}