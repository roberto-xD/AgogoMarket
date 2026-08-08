package com.passioagogo.market.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.passioagogo.market.R

val PoppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

/** Solo para logotipo y acentos manuscritos: nunca para texto de UI. */
val AlexBrushFamily = FontFamily(Font(R.font.alex_brush_regular, FontWeight.Normal))

/** Escala de marca, para usar explícitamente donde Material no alcanza. */
object PassionType {
    val Caption = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 1.68.sp,
    )
    val Small = TextStyle(fontFamily = PoppinsFamily, fontSize = 14.sp, lineHeight = 21.7.sp)
    val Body = TextStyle(fontFamily = PoppinsFamily, fontSize = 16.sp, lineHeight = 24.8.sp)
    val BodyLarge = TextStyle(fontFamily = PoppinsFamily, fontSize = 18.sp, lineHeight = 30.6.sp)
    val H5 = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp)
    val H4 = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp)
    val H3 = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 30.sp)
    val H2 = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        lineHeight = 43.7.sp,
    )
    val H1 = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 55.2.sp,
    )
    val Display = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold, fontSize = 72.sp)
    val ScriptAccent = TextStyle(fontFamily = AlexBrushFamily, fontSize = 52.sp)
}

/**
 * Los 15 estilos de Material 3 mapeados a Poppins: así toda la app hereda la
 * tipografía de marca sin tocar cada pantalla. Las escalas grandes se atenúan
 * respecto a PassionType (pensada para web) para que quepan en móvil.
 */
val PassionTypography = Typography(
    displayLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 55.sp),
    displayMedium = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 44.sp),
    displaySmall = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp),

    headlineLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp),

    titleLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),

    bodyLarge = TextStyle(fontFamily = PoppinsFamily, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PoppinsFamily, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = PoppinsFamily, fontSize = 12.sp, lineHeight = 18.sp),

    labelLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp),
)
