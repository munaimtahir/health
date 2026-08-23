package com.vexel.passport

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun initial_state_exposes_onboarding_or_labeled_primary_navigation() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Welcome to Vexel")).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodes(hasText("Health")).fetchSemanticsNodes().isNotEmpty()
        }
        val onboardingVisible = composeRule.onAllNodes(hasText("Welcome to Vexel")).fetchSemanticsNodes().isNotEmpty()
        val homeVisible = composeRule.onAllNodes(hasText("Health")).fetchSemanticsNodes().isNotEmpty()
        assertTrue("The app must expose onboarding or the primary navigation", onboardingVisible || homeVisible)
        if (homeVisible) {
            listOf("Health", "Timeline", "Plan", "Vault", "Profile").forEach { label ->
                assertTrue("Missing navigation label: $label", composeRule.onAllNodes(hasText(label)).fetchSemanticsNodes().isNotEmpty())
            }
            assertTrue(
                "Health navigation destination must expose a labeled click target",
                composeRule.onAllNodes(hasText("Health") and hasClickAction()).fetchSemanticsNodes().isNotEmpty(),
            )
        } else {
            composeRule.onNodeWithText("Continue").assertExists()
        }
    }
}
