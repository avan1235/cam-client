import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.shadow)
}

val entryPointPackage = "in.procyk.webcam.sender"
val entryPointSymbol = "$entryPointPackage.main"
val mainClassQName = "$entryPointPackage.MainKt"

kotlin {
    jvmToolchain(21)
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass = mainClassQName
            }
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }
    listOf(
        linuxX64(),
        linuxArm64(),
    ).forEach { target ->
        target.binaries.executable {
            entryPoint = entryPointSymbol
        }
    }
    listOf(
        macosArm64(),
    ).forEach { target ->
        target.binaries.executable {
            entryPoint = entryPointSymbol
            linkerOpts += "-L/Applications/Xcode-26.0.0.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/macosx"
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.sharedLogic)
            implementation(libs.ktor.client.websockets)
            implementation(libs.clikt)
            implementation(libs.qrcodegen)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        macosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        linuxMain.dependencies {
            implementation(libs.ktor.client.curl)
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = mainClassQName
    }
}