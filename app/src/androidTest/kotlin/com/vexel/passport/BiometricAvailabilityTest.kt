package com.vexel.passport

import androidx.biometric.BiometricManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricAvailabilityTest {
    @Test
    fun biometric_capability_is_handled_as_an_explicit_runtime_state() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )

        assertTrue(
            "Unexpected biometric availability code: $result",
            result in setOf(
                BiometricManager.BIOMETRIC_SUCCESS,
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            ),
        )
    }
}
