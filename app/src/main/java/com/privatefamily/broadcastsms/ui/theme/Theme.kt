package com.privatefamily.broadcastsms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LogoBlue,
    secondary = LogoGreen,
    tertiary = LogoSky,
    background = LogoMist,
    surface = LogoWhite,
    onPrimary = LogoWhite,
    onSecondary = LogoNavyDeep,
    onTertiary = LogoNavyDeep,
    onBackground = LogoNavyDeep,
    onSurface = LogoNavyDeep,
)

private val DarkColors = darkColorScheme(
    primary = LogoSky,
    secondary = LogoMint,
    tertiary = LogoBlue,
    background = LogoNavyDeep,
    surface = LogoNavy,
    onPrimary = LogoNavyDeep,
    onSecondary = LogoNavyDeep,
    onTertiary = LogoWhite,
    onBackground = LogoWhite,
    onSurface = LogoWhite,
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
