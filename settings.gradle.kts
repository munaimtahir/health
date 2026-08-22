import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "health"
include(":app")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:files")
include(":core:model")
include(":core:notifications")
include(":core:security")
include(":core:testing")
include(":core:ui")
include(":feature:onboarding")
include(":feature:dashboard")
include(":feature:profile")
include(":feature:timeline")
include(":feature:symptoms")
include(":feature:records")
include(":feature:medications")
include(":feature:reminders")
include(":feature:appointments")
include(":feature:reports")
include(":feature:settings")
