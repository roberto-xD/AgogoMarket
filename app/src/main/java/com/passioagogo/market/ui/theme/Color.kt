package com.passioagogo.market.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.passioagogo.market.R

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object InventoryColors {

    // Colores primarios
    val primary: Color @Composable get() = colorResource(R.color.primary)
    val primaryLight: Color @Composable get() = colorResource(R.color.primary_light)
    val primaryDark: Color @Composable get() = colorResource(R.color.primary_dark)
    val onPrimary: Color @Composable get() = colorResource(R.color.on_primary)
    val primaryContainer: Color @Composable get() = colorResource(R.color.primary_container)
    val onPrimaryContainer: Color @Composable get() = colorResource(R.color.on_primary_container)

    // Colores secundarios
    val secondary: Color @Composable get() = colorResource(R.color.secondary)
    val secondaryLight: Color @Composable get() = colorResource(R.color.secondary_light)
    val secondaryDark: Color @Composable get() = colorResource(R.color.secondary_dark)
    val onSecondary: Color @Composable get() = colorResource(R.color.on_secondary)
    val secondaryContainer: Color @Composable get() = colorResource(R.color.secondary_container)
    val onSecondaryContainer: Color @Composable get() = colorResource(R.color.on_secondary_container)

    // Colores terciarios
    val tertiary: Color @Composable get() = colorResource(R.color.tertiary)
    val onTertiary: Color @Composable get() = colorResource(R.color.on_tertiary)
    val tertiaryContainer: Color @Composable get() = colorResource(R.color.tertiary_container)
    val onTertiaryContainer: Color @Composable get() = colorResource(R.color.on_tertiary_container)

    // Colores de superficie
    val background: Color @Composable get() = colorResource(R.color.background)
    val onBackground: Color @Composable get() = colorResource(R.color.on_background)
    val surface: Color @Composable get() = colorResource(R.color.surface)
    val onSurface: Color @Composable get() = colorResource(R.color.on_surface)
    val surfaceVariant: Color @Composable get() = colorResource(R.color.surface_variant)
    val onSurfaceVariant: Color @Composable get() = colorResource(R.color.on_surface_variant)

    // Colores de estado
    val error: Color @Composable get() = colorResource(R.color.error)
    val onError: Color @Composable get() = colorResource(R.color.on_error)
    val errorContainer: Color @Composable get() = colorResource(R.color.error_container)
    val onErrorContainer: Color @Composable get() = colorResource(R.color.on_error_container)

    // Colores adicionales
    val outline: Color @Composable get() = colorResource(R.color.outline)
    val outlineVariant: Color @Composable get() = colorResource(R.color.outline_variant)
    val scrim: Color @Composable get() = colorResource(R.color.scrim)
    val inverseSurface: Color @Composable get() = colorResource(R.color.inverse_surface)
    val inverseOnSurface: Color @Composable get() = colorResource(R.color.inverse_on_surface)
    val inversePrimary: Color @Composable get() = colorResource(R.color.inverse_primary)

    // Colores de estado personalizados
    val success: Color @Composable get() = colorResource(R.color.success)
    val warning: Color @Composable get() = colorResource(R.color.warning)
    val info: Color @Composable get() = colorResource(R.color.info)

    // Colores de acento
    val accentBlue: Color @Composable get() = colorResource(R.color.accent_blue)
    val accentPurple: Color @Composable get() = colorResource(R.color.accent_purple)
    val accentPink: Color @Composable get() = colorResource(R.color.accent_pink)
    val accentOrange: Color @Composable get() = colorResource(R.color.accent_orange)

    // Grises neutros
    val gray50: Color @Composable get() = colorResource(R.color.gray_50)
    val gray100: Color @Composable get() = colorResource(R.color.gray_100)
    val gray200: Color @Composable get() = colorResource(R.color.gray_200)
    val gray300: Color @Composable get() = colorResource(R.color.gray_300)
    val gray400: Color @Composable get() = colorResource(R.color.gray_400)
    val gray500: Color @Composable get() = colorResource(R.color.gray_500)
    val gray600: Color @Composable get() = colorResource(R.color.gray_600)
    val gray700: Color @Composable get() = colorResource(R.color.gray_700)
    val gray800: Color @Composable get() = colorResource(R.color.gray_800)
    val gray900: Color @Composable get() = colorResource(R.color.gray_900)
}