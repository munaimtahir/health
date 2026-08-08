package com.vexel.passport.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

private val VexelTeal = Color(0xFF0F766E)
private val VexelTealContainer = Color(0xFFCCFBF1)
private val VexelSlate = Color(0xFF475569)
private val VexelLightBackground = Color(0xFFF8FAFC)
private val VexelLightSurface = Color(0xFFFFFFFF)
private val VexelDarkBackground = Color(0xFF0F172A)
private val VexelDarkSurface = Color(0xFF1E293B)
private val VexelDarkSurfaceVariant = Color(0xFF334155)

@Composable
fun VexelHealthPassportTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = VexelTeal,
        onPrimary = Color.White,
        primaryContainer = VexelTealContainer,
        onPrimaryContainer = Color(0xFF134E4A),
        secondary = VexelSlate,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE2E8F0),
        onSecondaryContainer = Color(0xFF1E293B),
        background = VexelLightBackground,
        onBackground = Color(0xFF0F172A),
        surface = VexelLightSurface,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = VexelSlate,
        outline = Color(0xFF64748B),
        outlineVariant = Color(0xFFE2E8F0),
        error = Color(0xFFB91C1C),
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFF5EEAD4),
        onPrimary = Color(0xFF003735),
        primaryContainer = Color(0xFF115E59),
        onPrimaryContainer = Color(0xFFCCFBF1),
        secondary = Color(0xFFCBD5E1),
        onSecondary = Color(0xFF1E293B),
        secondaryContainer = Color(0xFF334155),
        onSecondaryContainer = Color(0xFFE2E8F0),
        background = VexelDarkBackground,
        onBackground = Color(0xFFF8FAFC),
        surface = VexelDarkSurface,
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = VexelDarkSurfaceVariant,
        onSurfaceVariant = Color(0xFFCBD5E1),
        outline = Color(0xFF64748B),
        outlineVariant = Color(0xFF334155),
        error = Color(0xFFFCA5A5),
    )
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = Typography().let { typography ->
            typography.copy(
                headlineLarge = typography.headlineLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                headlineSmall = typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                titleMedium = typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                bodyLarge = typography.bodyLarge.copy(lineHeight = 24.sp),
            )
        },
        content = content,
    )
}
