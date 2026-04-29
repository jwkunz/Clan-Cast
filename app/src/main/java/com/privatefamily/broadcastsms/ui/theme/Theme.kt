package com.privatefamily.broadcastsms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Ink,
    secondary = Sage,
    tertiary = Sun,
    background = Cream,
    surface = Cream,
)

private val DarkColors = darkColorScheme(
    primary = Sand,
    secondary = Sage,
    tertiary = Sun,
)

@Composable
fun ClanCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
