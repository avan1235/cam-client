import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    dependencies {
        implementation(projects.sharedUi)
        implementation(libs.ktor.client.okhttp)

        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutinesSwing)

        implementation(compose.components.uiToolingPreview)
    }
}

compose.desktop {
    application {
        mainClass = "in.procyk.webcam.client.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "in.procyk.webcam.client"
            packageVersion = "1.0.0"
        }
    }
}