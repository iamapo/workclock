import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    androidTarget { compilerOptions.jvmTarget.set(JvmTarget.JVM_11) }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64())
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
        implementation("io.insert-koin:koin-core:4.1.1")
    }
}

android {
    namespace = "com.iamapo.timetracker.feature.calendar"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
