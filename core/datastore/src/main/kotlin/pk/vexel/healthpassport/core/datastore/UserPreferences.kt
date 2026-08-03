package pk.vexel.healthpassport.core.datastore

data class UserPreferences(
    val darkTheme: Boolean = false,
    val onboardingComplete: Boolean = false,
    val pinMaterial: String = "",
) {
    val lockEnabled: Boolean get() = pinMaterial.isNotBlank()
}
