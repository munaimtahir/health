package com.vexel.passport

import org.junit.Assert.assertEquals
import org.junit.Test

class AppShellTest {
    @Test
    fun primaryDestinationsMatchCanonicalOrderAndRoutes() {
        assertEquals(listOf("Health", "Timeline", "+", "Vault", "Profile"), primaryDestinationLabels)
    }

    @Test
    fun routeConstantsAreUniqueAndStable() {
        val routes = listOf(Routes.HOME, Routes.RECORDS, Routes.ADD, Routes.VAULT, Routes.PROFILE)
        assertEquals(routes.size, routes.toSet().size)
        assertEquals(listOf("home", "records", "add", "vault", "profile"), routes)
    }
}
