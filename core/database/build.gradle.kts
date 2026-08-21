plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android { namespace = "com.vexel.passport.database"; compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    lint { abortOnError = true }
    sourceSets { getByName("androidTest") { assets.srcDirs("$projectDir/schemas") } }
}
dependencies { api(project(":core:model")) }
dependencies { api(libs.androidx.room.runtime); api(libs.androidx.room.ktx); ksp(libs.androidx.room.compiler) }
dependencies { androidTestImplementation(libs.androidx.room.testing) }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }
