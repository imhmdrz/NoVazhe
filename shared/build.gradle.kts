import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    // Suppress expect/actual classes Beta warning
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        commonTest.dependencies { implementation(libs.kotlin.test) }
        commonMain.dependencies {
            // Network
            implementation(libs.ktor.core)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.xml)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Logger
            implementation(libs.napier)
            // JSON
            implementation(libs.kotlinx.serialization.json)
            // Key-Value storage
            implementation(libs.multiplatform.settings)
            // DI
            api(libs.koin.core)

            // Date formatting
            implementation(libs.kotlinx.datetime)

            // XML
            implementation(libs.xml.serialization)
            implementation(libs.xml.serialization.core)
        }
        androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
        iosMain.dependencies { implementation(libs.ktor.client.ios) }
        jvmMain.dependencies { implementation(libs.ktor.client.okhttp) }
    }
}

android {
    namespace = "mohaamadreza.saemipour.no.vazheh.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}
