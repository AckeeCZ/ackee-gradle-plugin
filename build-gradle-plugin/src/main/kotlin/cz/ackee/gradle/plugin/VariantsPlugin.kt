package cz.ackee.gradle.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import cz.ackee.gradle.PropertiesExtensionKotlin
import cz.ackee.gradle.type.CustomBuildTypeCreator
import cz.ackee.gradle.type.CustomBuildTypeFactory
import cz.ackee.gradle.type.CustomSigningConfigCreator
import cz.ackee.gradle.type.CustomSigningConfigFactory
import cz.ackee.gradle.util.getApplicationAndroidComponents
import cz.ackee.gradle.util.loadPropertiesFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import java.util.Properties

class VariantsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents = project.getApplicationAndroidComponents()
        val versionCodeProvider = VersionCodeProvider(project)
        with(androidComponents) {
            createSignings(project)
            createBuildTypes(versionCodeProvider.getVersionCode())
        }
    }

    private fun ApplicationAndroidComponentsExtension.createSignings(project: Project) {
        val keystoreProperties = parseKeystoreProperties(project)
        val factory = CustomSigningConfigFactory(project, keystoreProperties)
        val customSigningConfigs = factory.createSigningConfigs()

        finalizeDsl { application ->
            val creator = CustomSigningConfigCreator(application.signingConfigs)
            customSigningConfigs.forEach(creator::maybeCreate)
        }
    }

    private fun parseKeystoreProperties(project: Project): Properties {
        val keystorePropertiesExt = project.extensions.create(
            "keystoreProperties",
            PropertiesExtensionKotlin::class.java,
            project,
            "keystore.properties"
        )
        val keystoreProperties by project.extra(Properties().apply {
            val keystorePropertiesFile = project.file(keystorePropertiesExt.fullPath)
            if (keystorePropertiesFile.exists()) {
                loadPropertiesFile(keystorePropertiesFile)
            }
        })
        return keystoreProperties
    }

    private fun ApplicationAndroidComponentsExtension.createBuildTypes(versionCode: Int) {
        val factory = CustomBuildTypeFactory(versionCode)
        val customBuildTypes = factory.createBuildTypes()
        finalizeDsl { application ->
            val creator = CustomBuildTypeCreator(application)
            customBuildTypes.forEach(creator::maybeCreate)
        }
    }
}
