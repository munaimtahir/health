plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android { namespace = "com.vexel.passport.ui"; compileSdk = 36
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    composeOptions { }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    lint { abortOnError = true }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
dependencies { api(project(":core:designsystem")); api(project(":core:model")) }
dependencies {
    implementation(libs.androidx.activity.compose)
}
