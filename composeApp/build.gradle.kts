import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":feature:backup"))
            export(project(":feature:lockscreen"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:calendar"))
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:design"))
            api(project(":feature:backup"))
            api(project(":feature:lockscreen"))
            implementation(libs.koin.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.components.resources)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.iamapo.timetracker.shared"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig { minSdk = libs.versions.min.sdk.get().toInt() }

    testOptions {
        // Common mapper tests resolve Compose Multiplatform resources and run on Native.
        // Android's local JVM resource environment cannot execute those resource calls.
        unitTests.all { it.enabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}
