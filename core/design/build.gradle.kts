import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget { compilerOptions.jvmTarget.set(JvmTarget.JVM_11) }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64())
    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
        implementation("org.jetbrains.compose.foundation:foundation:1.10.3")
        implementation("org.jetbrains.compose.material3:material3:1.9.0")
        implementation("org.jetbrains.compose.ui:ui:1.10.3")
    }
}

android {
    namespace = "com.iamapo.timetracker.core.design"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
