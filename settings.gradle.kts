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
include(":feature:backup")
include(":core:design")
include(":core:resources")
include(":core:domain")
include(":core:data")
include(":feature:calendar")
include(":feature:timetracking")
include(":feature:settings")
