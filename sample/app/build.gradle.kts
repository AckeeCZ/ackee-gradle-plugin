import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.android.build.gradle.internal.dsl.ProductFlavor
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import java.util.Properties

plugins {
    id("cz.ackee.build")
    id("com.android.application")
}

android {
    val gitCommitsCount: Int by extra
    val appProperties: Properties by extra

    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28)
        applicationId = appProperties["package_name"] as String
        versionName = appProperties["version_name"] as String
        versionCode = gitCommitsCount

        addManifestPlaceholders(mapOf(
                "appName" to "Gradle build"
        ))

        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }
    }

    buildTypes {
        val hockeyAppId = "HOCKEYAPP_ID"
        val hockeyAppAutoSend = "HOCKEYAPP_AUTOSEND"

        maybeCreate("beta")

        getByName("debug") {
            buildConfigField("String", hockeyAppId, "\"aaaa\"")
            buildConfigField("Boolean", hockeyAppAutoSend, "false")
        }
        getByName("beta") {
            buildConfigField("String", hockeyAppId, "\"bbbb\"")
            buildConfigField("Boolean", hockeyAppAutoSend, "true")
        }
        getByName("release") {
            buildConfigField("String", hockeyAppId, "\"cccc\"")
            buildConfigField("Boolean", hockeyAppAutoSend, "true")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))
    implementation("com.android.support:appcompat-v7:28.0.0")
}
