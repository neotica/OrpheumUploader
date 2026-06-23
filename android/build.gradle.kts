plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "id.neotica.orpheum.uploader"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.neotica.orpheum.uploader"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES}"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("io.insert-koin:koin-android:4.0.1")
}
