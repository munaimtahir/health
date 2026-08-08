package com.vexel.passport.core.datastore

val supportedLockTimeoutMinutes = setOf(0, 5, 15, 30)

fun isSupportedLockTimeoutMinutes(minutes: Int): Boolean = minutes in supportedLockTimeoutMinutes

data class UserPreferences(
    val darkTheme: Boolean = false,
    val onboardingComplete: Boolean = false,
    val pinMaterial: String = "",
    val lockTimeoutMinutes: Int = 0,
) {
    val lockEnabled: Boolean get() = pinMaterial.isNotBlank()
}
