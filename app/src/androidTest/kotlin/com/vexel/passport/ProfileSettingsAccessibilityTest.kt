package com.vexel.passport

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileSettingsAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun profile_exposes_privacy_security_help_and_data_actions() {
        val onboarding = composeRule.onAllNodes(hasText("Welcome to Vexel")).fetchSemanticsNodes().isNotEmpty()
        if (onboarding) {
            composeRule.onNodeWithText("I understand and want to continue").performClick()
            composeRule.onNodeWithText("Continue").performClick()
            composeRule.waitForIdle()
        }
        val home = composeRule.onAllNodes(hasText("Home")).fetchSemanticsNodes().isNotEmpty()
        assertTrue("Home must be reachable before profile acceptance checks", home)
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Personal profile").assertExists()
        scrollUntilVisible("Reports and data tools")
        scrollUntilVisible("Appearance and security")
        scrollUntilVisible("Privacy and data")
        scrollUntilVisible("Help and about")
        scrollUntilVisible("Delete all app data")
    }

    // Raw swipe gestures on the LazyColumn are unreliable here: with six stacked
    // OutlinedTextFields, a synthetic swipeUp() computed from the list's full bounds
    // often lands its touch-down directly on a field, which consumes the drag instead
    // of the list. performScrollToNode drives the LazyColumn's scroll state directly
    // instead of simulating touch, sidestepping that collision entirely.
    private fun scrollUntilVisible(text: String) {
        composeRule.onNodeWithTag("profileScroll").performScrollToNode(hasText(text))
        composeRule.onNodeWithText(text).assertExists()
    }
}
