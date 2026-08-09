package com.vexel.passport.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

internal object VexelColors {
    const val TEAL: Long = 0xFF0F766E
    const val TEAL_CONTAINER: Long = 0xFFCCFBF1
    const val SLATE: Long = 0xFF475569
    const val LIGHT_BACKGROUND: Long = 0xFFF8FAFC
    const val LIGHT_SURFACE: Long = 0xFFFFFFFF
    const val LIGHT_TEXT: Long = 0xFF0F172A
    const val LIGHT_CONTAINER_TEXT: Long = 0xFF134E4A
    const val LIGHT_SECONDARY_CONTAINER: Long = 0xFFE2E8F0
    const val LIGHT_SECONDARY_CONTAINER_TEXT: Long = 0xFF1E293B
    const val LIGHT_ERROR: Long = 0xFFB91C1C
    const val DARK_BACKGROUND: Long = 0xFF0F172A
    const val DARK_SURFACE: Long = 0xFF1E293B
    const val DARK_SURFACE_VARIANT: Long = 0xFF334155
    const val DARK_TEXT: Long = 0xFFF8FAFC
    const val DARK_VARIANT_TEXT: Long = 0xFFCBD5E1
    const val DARK_PRIMARY: Long = 0xFF5EEAD4
    const val DARK_PRIMARY_CONTAINER: Long = 0xFF115E59
    const val DARK_PRIMARY_CONTAINER_TEXT: Long = 0xFFCCFBF1
    const val DARK_SECONDARY_CONTAINER_TEXT: Long = 0xFFE2E8F0
    const val DARK_ERROR: Long = 0xFFFCA5A5
}

internal fun contrastRatio(foreground: Long, background: Long): Double {
    fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val value = ((argb shr shift) and 0xFF).toDouble() / 255.0
            return if (value <= 0.04045) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
    val first = luminance(foreground)
    val second = luminance(background)
    return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
}

@Composable
fun VexelHealthPassportTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = Color(VexelColors.TEAL),
        onPrimary = Color.White,
        primaryContainer = Color(VexelColors.TEAL_CONTAINER),
        onPrimaryContainer = Color(VexelColors.LIGHT_CONTAINER_TEXT),
        secondary = Color(VexelColors.SLATE),
        onSecondary = Color.White,
        secondaryContainer = Color(VexelColors.LIGHT_SECONDARY_CONTAINER),
        onSecondaryContainer = Color(VexelColors.LIGHT_SECONDARY_CONTAINER_TEXT),
        background = Color(VexelColors.LIGHT_BACKGROUND),
        onBackground = Color(VexelColors.LIGHT_TEXT),
        surface = Color(VexelColors.LIGHT_SURFACE),
        onSurface = Color(VexelColors.LIGHT_TEXT),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(VexelColors.SLATE),
        outline = Color(0xFF64748B),
        outlineVariant = Color(0xFFE2E8F0),
        error = Color(VexelColors.LIGHT_ERROR),
    )
    val darkColors = darkColorScheme(
        primary = Color(VexelColors.DARK_PRIMARY),
        onPrimary = Color(0xFF003735),
        primaryContainer = Color(VexelColors.DARK_PRIMARY_CONTAINER),
        onPrimaryContainer = Color(VexelColors.DARK_PRIMARY_CONTAINER_TEXT),
        secondary = Color(VexelColors.DARK_VARIANT_TEXT),
        onSecondary = Color(0xFF1E293B),
        secondaryContainer = Color(VexelColors.DARK_SURFACE_VARIANT),
        onSecondaryContainer = Color(VexelColors.DARK_SECONDARY_CONTAINER_TEXT),
        background = Color(VexelColors.DARK_BACKGROUND),
        onBackground = Color(VexelColors.DARK_TEXT),
        surface = Color(VexelColors.DARK_SURFACE),
        onSurface = Color(VexelColors.DARK_TEXT),
        surfaceVariant = Color(VexelColors.DARK_SURFACE_VARIANT),
        onSurfaceVariant = Color(VexelColors.DARK_VARIANT_TEXT),
        outline = Color(0xFF64748B),
        outlineVariant = Color(0xFF334155),
        error = Color(VexelColors.DARK_ERROR),
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
