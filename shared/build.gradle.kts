import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch")
val fxPlatform = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> if (osArch == "aarch64") "mac-aarch64" else "mac"
    osName.contains("nux") || osName.contains("nix") -> "linux"
    else -> "win"
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.neotoast)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.koin)
            implementation("net.dongliu:apk-parser:2.6.10")
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.openjfx:javafx-base:19.0.2:$fxPlatform")
            implementation("org.openjfx:javafx-graphics:19.0.2:$fxPlatform")
            implementation("org.openjfx:javafx-media:19.0.2:$fxPlatform")
            implementation("org.openjfx:javafx-swing:19.0.2:$fxPlatform")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}


compose.desktop {
    application {
        mainClass = "id.neotica.orpheum.uploader.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "id.neotica.orpheum.uploader"
            packageVersion = "1.0.0"
        }
    }
}

buildConfig {
    packageName("id.neotica.orpheum.uploader.admin.config")

    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    fun getCleanConfig(key: String): String {
        val rawValue = properties.getProperty(key) ?: System.getenv(key) ?: ""

        val cleanValue = rawValue.removeSurrounding("\"").removeSurrounding("'")
        return "\"$cleanValue\""
    }

    buildConfigField("String", "BASE_URL", getCleanConfig("BASE_URL"))
    buildConfigField("String", "BASE_URL_BUCKET", getCleanConfig("BASE_URL_BUCKET"))
    buildConfigField("String", "BASE_URL_BUCKET_PUBLIC", getCleanConfig("BASE_URL_BUCKET_PUBLIC"))
}