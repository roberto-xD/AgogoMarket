package com.passioagogo.market.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta de marca Passion A Gogo.
 * Los tokens semánticos (SurfacePage, TextBody, BorderSubtle…) son la vía
 * preferente de consumo; las rampas numéricas quedan para casos puntuales.
 */
object PassionColors {
    // Rampa morada
    val Purple50 = Color(0xFFFBF4FC)
    val Purple100 = Color(0xFFF4E3F8)
    val Purple200 = Color(0xFFE6C2EF)
    val Purple300 = Color(0xFFD394E4)
    val Purple400 = Color(0xFFB85CD2)
    val Purple500 = Color(0xFF9E35C0)
    val Purple600 = Color(0xFF8E22BB) // primario de marca
    val Purple700 = Color(0xFF711C97)
    val Purple800 = Color(0xFF551774)
    val Purple900 = Color(0xFF360F4D)

    // Rosa / acento
    val Pink50 = Color(0xFFFFF0F6)
    val Pink100 = Color(0xFFFFD9E9)
    val Pink200 = Color(0xFFFFB0D2)
    val Pink300 = Color(0xFFFF7FB4)
    val Pink400 = Color(0xFFF94F97)
    val Pink500 = Color(0xFFE8317E)
    val Pink600 = Color(0xFFC41F66)
    val Pink700 = Color(0xFF9C1852)

    // Neutros cálidos
    val Neutral0 = Color(0xFFFFFDFE)
    val Neutral50 = Color(0xFFFBF6FA)
    val Neutral100 = Color(0xFFF3EAF1)
    val Neutral200 = Color(0xFFE4D5E2)
    val Neutral300 = Color(0xFFC9B3C7)
    val Neutral400 = Color(0xFF9E8298)
    val Neutral500 = Color(0xFF786274)
    val Neutral600 = Color(0xFF5B4A58)
    val Neutral700 = Color(0xFF40323D)
    val Neutral800 = Color(0xFF2B1F29)
    val Neutral900 = Color(0xFF1A1218)

    // Semánticos
    val Success = Color(0xFF3E9B6B)
    val SuccessBg = Color(0xFFE8F7EF)
    val Warning = Color(0xFFD98A2B)
    val WarningBg = Color(0xFFFBF0DF)
    val Error = Color(0xFFD6455F)
    val ErrorBg = Color(0xFFFCE7EA)

    // Alias semánticos
    val SurfacePage = Neutral50
    val SurfaceCard = Neutral0
    val SurfaceSunken = Neutral100
    val SurfaceInverse = Purple900
    val TextHeading = Neutral900
    val TextBody = Neutral600
    val TextMuted = Neutral400
    val TextOnBrand = Neutral0
    val BorderSubtle = Neutral200
    val BorderDefault = Neutral300
    val BorderFocus = Purple500
}

val PassionLightColorScheme = lightColorScheme(
    primary = PassionColors.Purple600,
    onPrimary = PassionColors.TextOnBrand,
    primaryContainer = PassionColors.Purple100,
    onPrimaryContainer = PassionColors.Purple800,
    secondary = PassionColors.Pink500,
    onSecondary = Color.White,
    secondaryContainer = PassionColors.Pink100,
    onSecondaryContainer = PassionColors.Pink700,
    tertiary = PassionColors.Purple400,
    onTertiary = Color.White,
    background = PassionColors.SurfacePage,
    onBackground = PassionColors.TextHeading,
    surface = PassionColors.SurfaceCard,
    onSurface = PassionColors.TextHeading,
    surfaceVariant = PassionColors.SurfaceSunken,
    onSurfaceVariant = PassionColors.TextBody,
    surfaceContainer = PassionColors.Neutral50,
    surfaceContainerHigh = PassionColors.Neutral100,
    outline = PassionColors.BorderDefault,
    outlineVariant = PassionColors.BorderSubtle,
    error = PassionColors.Error,
    onError = Color.White,
    errorContainer = PassionColors.ErrorBg,
    onErrorContainer = PassionColors.Pink700,
    scrim = PassionColors.Neutral900,
)
