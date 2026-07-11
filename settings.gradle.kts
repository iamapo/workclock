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

rootProject.name = "WorkClock"
include(":composeApp")
include(":androidApp")
include(":feature:lockscreen")
include(":core:design")
include(":core:domain")
include(":core:data")
include(":feature:calendar")
