plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android { namespace = "com.vexel.passport.notifications"; compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    lint { abortOnError = true }
}
dependencies { api(project(":core:domain")); implementation(project(":core:database")) }
dependencies { implementation(libs.androidx.work.runtime); implementation(libs.androidx.core.ktx) }
