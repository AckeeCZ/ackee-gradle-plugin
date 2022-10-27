package cz.ackee.gradle.plugin

import com.android.build.gradle.AppExtension
import cz.ackee.gradle.PropertiesExtensionKotlin
import cz.ackee.gradle.getAndroidAppExtensionOrThrow
import cz.ackee.gradle.type.CustomBuildType
import cz.ackee.gradle.type.CustomSigningConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.Properties

class VariantsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val android = project.getAndroidAppExtensionOrThrow()
        android.createSignings(project)
        android.createVariants()
    }

    // TODO refactor keystore properties
    private fun AppExtension.createSignings(project: Project) {
        val keystorePropertiesExt = project.extensions.create(
            "keystoreProperties",
            PropertiesExtensionKotlin::class.java,
            project,
            "keystore.properties"
        )
        val keystoreProperties by project.extra(Properties().apply {
            val keystorePropertiesFile = project.file(keystorePropertiesExt.fullPath)
            if (keystorePropertiesFile.exists()) {
                loadKeystoreProperties(keystorePropertiesFile)
            }
        })
        val keystoreDir = File(project.rootDir, "keystore")

        val customSigningConfigs = listOf(
            CustomSigningConfig.Debug(keystoreDir),
            CustomSigningConfig.Release(
                keystoreDir = keystoreDir,
                storeFileName = keystoreProperties["key_file"] as String? ?: "",
                storePassword = keystoreProperties["key_password"] as String?,
                keyAlias = keystoreProperties["key_alias"] as String?,
                keyPassword = keystoreProperties["key_password"] as String?
            )
        )

        signingConfigs {
            customSigningConfigs.forEach { signingConfig ->
                signingConfig.maybeCreate(this)
            }
        }
    }

    private fun Properties.loadKeystoreProperties(keystorePropertiesFile: File): Properties {
        val fileReader = FileReader(keystorePropertiesFile)
        val bufferedReader = BufferedReader(fileReader)
        load(bufferedReader)
        return this
    }

    private fun AppExtension.createVariants() {
        val versionCode = defaultConfig.versionCode ?: 0
        val customBuildTypes = listOf(
            CustomBuildType.Debug,
            CustomBuildType.Beta(versionCode),
            CustomBuildType.Release
        )

        buildTypes {
            customBuildTypes.forEach { buildType ->
                buildType.maybeCreate(
                    container = this,
                    android = this@createVariants
                )
            }
        }
    }
}
