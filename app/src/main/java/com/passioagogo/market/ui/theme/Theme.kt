package com.passioagogo.market.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// ---------- Radios ----------
object PassionRadius {
    val Sm = 6.dp
    val Md = 12.dp
    val Lg = 20.dp
    val Xl = 28.dp
    val Pill = 999.dp
}

/**
 * Ojo: `extraLarge` NO puede ser Pill. Material 3 lo usa para diálogos y
 * hojas inferiores, y un radio de 999.dp los deforma en óvalos. La forma
 * de píldora se aplica explícitamente donde toca (botones, chips) con
 * [PillShape].
 */
val PassionShapes = Shapes(
    extraSmall = RoundedCornerShape(PassionRadius.Sm),
    small = RoundedCornerShape(PassionRadius.Md),
    medium = RoundedCornerShape(PassionRadius.Lg),
    large = RoundedCornerShape(PassionRadius.Xl),
    extraLarge = RoundedCornerShape(PassionRadius.Xl),
)

/** Forma de píldora de marca: `Button(shape = PillShape)`. */
val PillShape = RoundedCornerShape(PassionRadius.Pill)

// ---------- Espaciado ----------
object PassionSpacing {
    val s1 = 4.dp; val s2 = 8.dp; val s3 = 12.dp; val s4 = 16.dp; val s5 = 20.dp
    val s6 = 24.dp; val s8 = 32.dp; val s10 = 40.dp; val s12 = 48.dp; val s16 = 64.dp
    val s20 = 80.dp; val s24 = 96.dp
}

// ---------- Sombras tintadas ----------
// Uso: Modifier.shadow(PassionShadow.ElevationMd, ambientColor = PassionShadow.Tint,
//                      spotColor = PassionShadow.Tint)
object PassionShadow {
    val Tint = PassionColors.Purple900
    val ElevationSm = 2.dp
    val ElevationMd = 8.dp
    val ElevationLg = 20.dp
    val GlowColor = PassionColors.Purple600.copy(alpha = 0.14f)
}

/**
 * Tema de la app. Solo esquema claro: la identidad de Passion A Gogo se
 * apoya en superficies claras y cálidas, y un modo oscuro derivado
 * automáticamente distorsionaría la marca. Si más adelante hace falta,
 * se añade un darkColorScheme explícito y afinado.
 */
@Composable
fun PassioAgogoMarketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PassionLightColorScheme,
        shapes = PassionShapes,
        typography = PassionTypography,
        content = content,
    )
}
