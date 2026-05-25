package com.ascendai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Brand colors ─────────────────────────────────────────────────────────────

val PrimaryViolet     = Color(0xFF7F77DD)
val PrimaryVioletDark = Color(0xFF534AB7)
val PrimaryVioletLight= Color(0xFFAFA9EC)

val BackgroundDark    = Color(0xFF0A0A0A)
val SurfaceDark       = Color(0xFF111111)
val CardDark          = Color(0xFF1A1A1A)
val BorderDark        = Color(0xFF2A2A2A)
val BorderSubtle      = Color(0xFF1F1F1F)

val TextPrimary       = Color(0xFFFFFFFF)
val TextSecondary     = Color(0xFF9A9A9A)
val TextTertiary      = Color(0xFF5A5A5A)

val SuccessGreen      = Color(0xFF1D9E75)
val WarningAmber      = Color(0xFFEF9F27)
val ErrorRed          = Color(0xFFE24B4A)

val GoogleBlue        = Color(0xFF4285F4)

// ─── Color scheme ─────────────────────────────────────────────────────────────

private val AscendDarkColorScheme = darkColorScheme(
    primary            = PrimaryViolet,
    onPrimary          = Color.White,
    primaryContainer   = PrimaryVioletDark,
    onPrimaryContainer = PrimaryVioletLight,
    secondary          = TextSecondary,
    onSecondary        = Color.Black,
    background         = BackgroundDark,
    onBackground       = TextPrimary,
    surface            = SurfaceDark,
    onSurface          = TextPrimary,
    surfaceVariant     = CardDark,
    onSurfaceVariant   = TextSecondary,
    outline            = BorderDark,
    error              = ErrorRed,
    onError            = Color.White
)

// ─── Theme composable ─────────────────────────────────────────────────────────

@Composable
fun AscendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AscendDarkColorScheme,
        typography  = AscendTypography,
        content     = content
    )
}
