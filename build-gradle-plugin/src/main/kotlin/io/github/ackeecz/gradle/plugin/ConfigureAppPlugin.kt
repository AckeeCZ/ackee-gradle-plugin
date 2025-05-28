package io.github.ackeecz.gradle.plugin

import io.github.ackeecz.gradle.PropertiesExtensionKotlin
import io.github.ackeecz.gradle.util.getApplicationAndroidComponents
import io.github.ackeecz.gradle.util.loadPropertiesFile
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

        @Suppress("UNUSED_VARIABLE")
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
