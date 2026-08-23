package com.vexel.passport

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccessibilityStructureTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun interactive_controls_on_all_primary_tabs_are_labeled_and_at_least_48dp() {
        reachHome()
        val minimumTouchTargetPx = 48f * composeRule.activity.resources.displayMetrics.density
        val navigationLabels = listOf("Health", "Timeline", "Plan", "Vault", "Profile")
        val navigationNodes = navigationLabels.map { label ->
            composeRule.onNode(hasText(label) and hasClickAction()).fetchSemanticsNode()
        }
        navigationNodes.forEachIndexed { index, node ->
            assertTrue(
                "${navigationLabels[index]} navigation target is smaller than 48dp",
                node.touchBoundsInRoot.width + 0.5f >= minimumTouchTargetPx &&
                    node.touchBoundsInRoot.height + 0.5f >= minimumTouchTargetPx,
            )
        }
        navigationLabels.forEach { tab ->
            composeRule.onNode(hasText(tab) and hasClickAction()).performClick()
            composeRule.waitForIdle()
            auditVisibleControls(tab, minimumTouchTargetPx)
        }

        // Profile is longer than a phone viewport, so exercise each action section rather than
        // treating LazyColumn's deliberately unplaced (0x0) semantics nodes as visible controls.
        listOf("Reports and data tools", "Appearance and security", "Privacy and data", "Help and about", "Delete all app data").forEach { label ->
            composeRule.onNodeWithTag("profileScroll").performScrollToNode(hasText(label))
            composeRule.waitForIdle()
            auditVisibleControls("Profile near $label", minimumTouchTargetPx)
        }
    }

    private fun auditVisibleControls(screen: String, minimumTouchTargetPx: Float) {
        val viewport = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes()
            .filter { it.boundsInRoot.width > 0f && it.boundsInRoot.height > 0f }
            .maxBy { it.boundsInRoot.width * it.boundsInRoot.height }
        val controls = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
            .filter {
                it.touchBoundsInRoot.width > 0f &&
                    it.touchBoundsInRoot.top >= viewport.boundsInRoot.top - 0.5f &&
                    it.touchBoundsInRoot.bottom <= viewport.boundsInRoot.bottom + 0.5f
            }
        assertTrue("$screen must expose at least one interactive control", controls.isNotEmpty())
        controls.forEachIndexed { index, node ->
            val text = if (node.config.contains(SemanticsProperties.Text)) {
                node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
            } else ""
            val descriptions = if (node.config.contains(SemanticsProperties.ContentDescription)) {
                node.config[SemanticsProperties.ContentDescription].joinToString(separator = "")
            } else ""
            assertTrue(
                "$screen interactive control $index has no accessible text or content description",
                text.isNotBlank() || descriptions.isNotBlank(),
            )
            assertTrue(
                "$screen interactive control $index ('$text$descriptions') is " +
                    "${node.touchBoundsInRoot.width}x${node.touchBoundsInRoot.height}px; " +
                    "minimum is ${minimumTouchTargetPx}px",
                node.touchBoundsInRoot.width + 0.5f >= minimumTouchTargetPx &&
                    node.touchBoundsInRoot.height + 0.5f >= minimumTouchTargetPx,
            )
        }
    }

    private fun reachHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("Welcome to Vexel")).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodes(hasText("Health")).fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodes(hasText("Welcome to Vexel")).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("I understand and want to continue").performClick()
            composeRule.onNodeWithText("Continue").performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodes(hasText("Health")).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
