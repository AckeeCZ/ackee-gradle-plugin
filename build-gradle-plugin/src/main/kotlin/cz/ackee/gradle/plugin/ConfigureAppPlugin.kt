package cz.ackee.gradle.plugin

import cz.ackee.gradle.PropertiesExtensionKotlin
import cz.ackee.gradle.getApplicationAndroidComponents
import cz.ackee.gradle.loadPropertiesFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import java.util.Properties

class ConfigureAppPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            provideAppProperties()
            setVersionCode()
        }
    }

    private fun Project.provideAppProperties() {
        val keystorePropertiesExt = extensions.create(
            "appProperties",
            PropertiesExtensionKotlin::class.java,
            this,
            "app.properties"
        )
        val appProperties by extra(Properties().apply {
            val keystorePropertiesFile = file(keystorePropertiesExt.fullPath)
            if (keystorePropertiesFile.exists()) {
                loadPropertiesFile(keystorePropertiesFile)
            }
        })
    }

    private fun Project.setVersionCode() {
        val versionCodeProvider = VersionCodeProvider(this)
        val versionCode = versionCodeProvider.getVersionCode()
        val androidComponents = getApplicationAndroidComponents()

        androidComponents.finalizeDsl {
            it.defaultConfig.versionCode = versionCode
        }
    }
}
