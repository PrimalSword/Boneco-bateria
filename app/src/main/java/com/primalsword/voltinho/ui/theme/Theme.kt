package com.primalsword.voltinho.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF09111F)
val NavySoft = Color(0xFF111D31)
val Lime = Color(0xFFA8FF35)
val Ice = Color(0xFFEAF2FF)
val Sky = Color(0xFF67C6FF)
val Coral = Color(0xFFFF6978)
val Amber = Color(0xFFFFDE44)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Navy,
    secondary = Sky,
    onSecondary = Navy,
    background = Navy,
    onBackground = Ice,
    surface = NavySoft,
    onSurface = Ice,
    error = Coral,
)

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Color(0xFF3A6E00),
    onSecondary = Color.White,
    background = Color(0xFFF5F8FC),
    onBackground = Navy,
    surface = Color.White,
    onSurface = Navy,
    error = Color(0xFFB3261E),
)

@Composable
fun VoltinhoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
