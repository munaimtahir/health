package com.vexel.passport

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.security.KeystorePinMaterialCipher
import com.vexel.passport.core.security.PinVerifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/**
 * Host-assisted acceptance test. Run only on an emulator with an enrolled virtual fingerprint,
 * pass `-e vexel.biometric.success true`, then issue `adb emu finger touch 1` while the prompt is
 * visible. The ordinary connected suite skips this test so an unenrolled physical device is not a
 * false failure.
 */
class BiometricUnlockSuccessTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun enrolled_device_authentication_success_unlocks_the_app() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(InstrumentationRegistry.getArguments().getString("vexel.biometric.success") == "true")
        val context = instrumentation.targetContext
        val preferencesStore = PreferencesStore(context)
        val original = preferencesStore.preferences.first()
        val pinMaterial = KeystorePinMaterialCipher().encrypt(PinVerifier().create("2468".toCharArray()))

        preferencesStore.setOnboardingComplete(true)
        preferencesStore.setPinMaterial(pinMaterial)
        preferencesStore.setLockTimeoutMinutes(0)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodes(hasText("Use device authentication")).fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Use device authentication").performClick()
            composeRule.waitUntil(timeoutMillis = 45_000) {
                composeRule.onAllNodes(hasText("Health")).fetchSemanticsNodes().isNotEmpty()
            }
        } finally {
            scenario.close()
            preferencesStore.setDarkTheme(original.darkTheme)
            preferencesStore.setOnboardingComplete(original.onboardingComplete)
            if (original.pinMaterial.isBlank()) preferencesStore.clearPinMaterial()
            else preferencesStore.setPinMaterial(original.pinMaterial)
            preferencesStore.setLockTimeoutMinutes(original.lockTimeoutMinutes)
        }
    }
}
