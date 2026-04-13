package com.aconcepcion.markerbuilder.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color.White,
    secondary = Color(0xFF0F9D58),
    tertiary = Color(0xFFE37400),
    surface = Color(0xFFF7F9FC),
    surfaceContainerHighest = Color(0xFFE5ECF6),
    background = Color(0xFFEAF1FF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFAAC7FF),
    secondary = Color(0xFF8DE7B7),
    tertiary = Color(0xFFFFBA7A),
    surface = Color(0xFF101418),
    surfaceContainerHighest = Color(0xFF1D2630),
    background = Color(0xFF0B1118),
)

@Composable
fun MarkerBuilderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
