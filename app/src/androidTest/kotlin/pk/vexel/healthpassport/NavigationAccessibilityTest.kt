package pk.vexel.healthpassport

import androidx.compose.ui.test.hasContentDescription
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
        val onboardingVisible = composeRule.onAllNodes(hasText("Welcome to Vexel")).fetchSemanticsNodes().isNotEmpty()
        val homeVisible = composeRule.onAllNodes(hasText("Home")).fetchSemanticsNodes().isNotEmpty()
        assertTrue("The app must expose onboarding or the primary navigation", onboardingVisible || homeVisible)
        if (homeVisible) {
            listOf("Home", "Records", "Plan", "Vault", "Profile").forEach { label ->
                assertTrue("Missing navigation label: $label", composeRule.onAllNodes(hasText(label)).fetchSemanticsNodes().isNotEmpty())
            }
            assertTrue("Home navigation icon must be labeled", composeRule.onAllNodes(hasContentDescription("Home")).fetchSemanticsNodes().isNotEmpty())
        } else {
            composeRule.onNodeWithText("Continue").assertExists()
        }
    }
}
