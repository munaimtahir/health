package pk.vexel.healthpassport

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
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
        composeRule.onNodeWithText("Reports and data tools").assertExists()
        repeat(4) { composeRule.onAllNodes(hasScrollAction()).onFirst().performTouchInput { swipeUp() } }
        composeRule.onNodeWithText("Appearance and security").assertExists()
        composeRule.onNodeWithText("Privacy and data").assertExists()
        composeRule.onNodeWithText("Help and about").assertExists()
        composeRule.onNodeWithText("Delete all app data").assertExists()
    }
}
