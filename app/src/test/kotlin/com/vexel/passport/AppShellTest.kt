package com.vexel.passport

import org.junit.Assert.assertEquals
import org.junit.Test

class AppShellTest {
    @Test
    fun primaryDestinationsMatchCanonicalOrderAndRoutes() {
        assertEquals(listOf("Home", "Records", "Plan", "Vault", "Profile"), primaryDestinationLabels)
    }

    @Test
    fun routeConstantsAreUniqueAndStable() {
        val routes = listOf(Routes.HOME, Routes.RECORDS, Routes.PLAN, Routes.VAULT, Routes.PROFILE)
        assertEquals(routes.size, routes.toSet().size)
        assertEquals(listOf("home", "records", "plan", "vault", "profile"), routes)
    }
}

