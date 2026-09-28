package com.kharcha.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Teal = Color(0xFF00796B)
private val TealDark = Color(0xFF004D40)
private val Amber = Color(0xFFFFA000)

private val LightColors = lightColorScheme(
    primary = Teal,
    secondary = Amber,
    tertiary = TealDark
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    secondary = Amber,
    tertiary = Color(0xFF80CBC4)
)

@Composable
fun KharchaTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
