package com.smartcart.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ColorScheme = lightColorScheme(
    primary             = Primary,
    onPrimary           = White,
    primaryContainer    = PrimaryLight,
    onPrimaryContainer  = PrimaryDark,
    background          = Background,
    onBackground        = TextPrimary,
    surface             = Surface,
    onSurface           = TextPrimary,
    surfaceVariant      = Gray100,
    onSurfaceVariant    = TextSecondary,
    outline             = Border,
    error               = ErrorRed,
)

val AppTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Black,    fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 10.sp, letterSpacing = 0.8.sp),
)

@Composable
fun SmartCartTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = AppTypography, content = content)
}
