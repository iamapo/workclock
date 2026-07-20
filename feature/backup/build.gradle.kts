import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64())

    sourceSets.commonMain.dependencies {
        implementation(project(":core:data"))
        implementation(project(":core:domain"))
        implementation(libs.compose.runtime)
        implementation(libs.kotlinx.datetime)
    }
}

android {
    namespace = "com.iamapo.timetracker.feature.backup"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig { minSdk = libs.versions.min.sdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
