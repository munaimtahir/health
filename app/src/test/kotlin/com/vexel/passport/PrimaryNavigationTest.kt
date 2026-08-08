package com.vexel.passport

import org.junit.Assert.assertEquals
import org.junit.Test

class PrimaryNavigationTest {
    @Test
    fun primary_navigation_uses_five_product_destinations_in_order() {
        assertEquals(listOf("Home", "Records", "Plan", "Vault", "Profile"), primaryDestinationLabels)
    }
}
