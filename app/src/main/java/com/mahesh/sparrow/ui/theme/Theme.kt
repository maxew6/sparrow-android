package com.mahesh.sparrow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SparrowBrown,
    onPrimary = Color.White,
    primaryContainer = SparrowCream,
    background = BackgroundLight,
    surface = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = SparrowCream,
    onPrimary = SparrowTailDark,
    primaryContainer = SparrowWingBrown,
    background = BackgroundDark,
    surface = SurfaceDark
)

@Composable
fun SparrowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SparrowTypography,
        content = content
    )
}
