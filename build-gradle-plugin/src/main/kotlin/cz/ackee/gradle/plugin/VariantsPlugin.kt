package cz.ackee.gradle.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import cz.ackee.gradle.PropertiesExtensionKotlin
import cz.ackee.gradle.getApplicationAndroidComponents
import cz.ackee.gradle.task.GetVersionCodeTask
import cz.ackee.gradle.type.CustomBuildTypeCreator
import cz.ackee.gradle.type.CustomBuildTypeFactory
import cz.ackee.gradle.type.CustomSigningConfigCreator
import cz.ackee.gradle.type.CustomSigningConfigFactory
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
        val androidComponents = project.getApplicationAndroidComponents()
        androidComponents.createSignings(project)
        val versionCodeTaskProvider = GetVersionCodeTask.registerTask(project)
        androidComponents.createBuildTypes(versionCodeTaskProvider.get().versionCodeFile.get().asFile.readText().toInt())
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
                loadKeystoreProperties(keystorePropertiesFile)
            }
        })
        return keystoreProperties
    }

    private fun Properties.loadKeystoreProperties(keystorePropertiesFile: File): Properties {
        val fileReader = FileReader(keystorePropertiesFile)
        val bufferedReader = BufferedReader(fileReader)
        load(bufferedReader)
        return this
    }

    private fun ApplicationAndroidComponentsExtension.createBuildTypes(versionCode: Int) {
        val factory = CustomBuildTypeFactory(versionCode)
        val customBuildTypes = factory.createBuildTypes()
        finalizeDsl { application ->
            val creator = CustomBuildTypeCreator(application.buildTypes, application.signingConfigs)
            customBuildTypes.forEach(creator::maybeCreate)
        }
    }
}
