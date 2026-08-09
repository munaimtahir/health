package com.vexel.passport.core.designsystem

import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {
    @Test
    fun body_action_error_and_status_pairs_meet_wcag_aa() {
        val pairs = mapOf(
            "light body" to (VexelColors.LIGHT_TEXT to VexelColors.LIGHT_BACKGROUND),
            "light secondary body" to (VexelColors.SLATE to VexelColors.LIGHT_SURFACE),
            "light primary action" to (VexelColors.LIGHT_SURFACE to VexelColors.TEAL),
            "light primary container" to (VexelColors.LIGHT_CONTAINER_TEXT to VexelColors.TEAL_CONTAINER),
            "light status" to (VexelColors.LIGHT_SECONDARY_CONTAINER_TEXT to VexelColors.LIGHT_SECONDARY_CONTAINER),
            "light error" to (VexelColors.LIGHT_ERROR to VexelColors.LIGHT_SURFACE),
            "dark body" to (VexelColors.DARK_TEXT to VexelColors.DARK_BACKGROUND),
            "dark secondary body" to (VexelColors.DARK_VARIANT_TEXT to VexelColors.DARK_SURFACE),
            "dark primary action" to (VexelColors.DARK_PRIMARY to VexelColors.DARK_BACKGROUND),
            "dark primary container" to (VexelColors.DARK_PRIMARY_CONTAINER_TEXT to VexelColors.DARK_PRIMARY_CONTAINER),
            "dark status" to (VexelColors.DARK_SECONDARY_CONTAINER_TEXT to VexelColors.DARK_SURFACE_VARIANT),
            "dark error" to (VexelColors.DARK_ERROR to VexelColors.DARK_SURFACE),
        )

        pairs.forEach { (name, colors) ->
            val ratio = contrastRatio(colors.first, colors.second)
            assertTrue("$name contrast was $ratio, expected at least 4.5:1", ratio >= 4.5)
        }
    }
}
