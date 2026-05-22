package com.alaa.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Islamic gold palette ────────────────────────────────────────────────────
val Gold         = Color(0xFFC9A84C)
val GoldLight    = Color(0xFFEDE0C4)
val GoldDark     = Color(0xFF8B6914)
val IslamicGreen = Color(0xFF00695C)
val DarkBg       = Color(0xFF0A1628)
val DarkBg2      = Color(0xFF0D1F3A)
val TextLight    = Color(0xFFE8F4F0)

private val DarkColorScheme = darkColorScheme(
    primary        = Gold,
    onPrimary      = Color.Black,
    secondary      = IslamicGreen,
    onSecondary    = Color.White,
    background     = DarkBg,
    onBackground   = TextLight,
    surface        = DarkBg2,
    onSurface      = TextLight,
    primaryContainer    = GoldLight,
    onPrimaryContainer  = Color.Black,
)

@Composable
fun AlaAppTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor  = DarkBg.toArgb()
            window.navigationBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
