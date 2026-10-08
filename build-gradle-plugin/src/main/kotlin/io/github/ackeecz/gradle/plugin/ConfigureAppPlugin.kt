package io.github.ackeecz.gradle.plugin

import io.github.ackeecz.gradle.PropertiesExtensionKotlin
import io.github.ackeecz.gradle.util.getApplicationAndroidComponents
import io.github.ackeecz.gradle.util.loadProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

class ConfigureAppPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            provideAppProperties()
            setVersionCode()
        }
    }

    private fun Project.provideAppProperties() {
        val appPropertiesExt = extensions.create(
            "appProperties",
            PropertiesExtensionKotlin::class.java,
            this,
            "app.properties"
        )

        extra.set("appProperties", loadProperties(appPropertiesExt.fullPath))
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
