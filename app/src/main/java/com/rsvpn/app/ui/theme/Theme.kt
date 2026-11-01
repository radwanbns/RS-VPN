package com.rsvpn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RsVpnColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = BackgroundDark,
    secondary = PrimaryCyanDark,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    outline = SurfaceBorder,
)

@Composable
fun RsVpnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // RS VPN تستخدم ثيم داكن دائمًا بصرف النظر عن إعداد النظام
    MaterialTheme(
        colorScheme = RsVpnColorScheme,
        typography = RsVpnTypography,
        content = content,
    )
}
