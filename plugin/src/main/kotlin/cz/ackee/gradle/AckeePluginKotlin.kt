package cz.ackee.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.util.Properties

class AckeePluginKotlin : Plugin<Project> {

    companion object {
        fun getGitCommitsCount(project: Project): Int {
            val stdout = ByteArrayOutputStream()
            project.exec { spec ->
                spec.commandLine = listOf("git", "rev-list", "HEAD", "--count")
                spec.standardOutput = stdout
            }
            return Integer.parseInt(stdout.toString().trim())
        }
    }

    override fun apply(project: Project) {
        val android = project.extensions.findByType(AppExtension::class.java) ?: throw Exception(
                "Not an Android application. Did you forget to apply `'com.android.application` plugin?"
        )

        /**
         * Define properties with keystore info
         */
        val keystorePropertiesExt = project.extensions.create("keystoreProperties", PropertiesExtensionKotlin::class.java, project, "keystore.properties")
        val keystoreProperties = Properties()
        keystoreProperties.load(BufferedReader(FileReader(project.file(keystorePropertiesExt.fullPath))))
        project.extensions.extraProperties.set("keystoreProperties", keystoreProperties)

        /**
         * Define properties with application info
         */
        val appPropertiesExt = project.extensions.create("appProperties", PropertiesExtensionKotlin::class.java, project, "app.properties")
        val appProperties = Properties()
        keystoreProperties.load(BufferedReader(FileReader(project.file(appPropertiesExt.fullPath))))
        project.extensions.extraProperties.set("appProperties", appProperties)

//        project.beforeEvaluate {
//            /**
//             * Get count of git commits
//             */
//            project.extensions.extraProperties.set("gitCommitsCount", getGitCommitsCount(project))
//            project.extensions.extraProperties.set("getGitCommitsCount", { getGitCommitsCount(project) })
//        }
        project.afterEvaluate {

            /**
             * Set output apk destination to file App.apk in outputs folder in project root
             */
            android.applicationVariants.all { variant ->
                val outputs = File(project.rootDir, "outputs")
                outputs.mkdir()
                val apkFile = File(outputs, "App.apk")

                variant.outputs.forEach { output: BaseVariantOutput ->
                    val taskName = "copyAndRename${variant.name.capitalize()}APK"
                    val copyAndRenameAPKTask = project.tasks.create(taskName, Copy::class.java) { task ->
                        task.from(output.outputFile.parent)
                        task.into(task.outputs)
                        task.include(output.outputFile.name)
                        task.rename(output.outputFile.name, apkFile.name)
                    }

                    // if copyAndRenameAPKTask needs to automatically execute assemble before
                    copyAndRenameAPKTask.dependsOn(variant.assemble)
                    copyAndRenameAPKTask.mustRunAfter(variant.assemble)

                    // if assemble needs to automatically execute copyAndRenameAPKTask after
                    variant.assemble.finalizedBy(copyAndRenameAPKTask)
                }
            }

            /**
             * Copy mapping.txt from its location to outputs folder in project root
             */
            android.applicationVariants.all { variant ->
                if (variant.buildType.isMinifyEnabled) {
                    variant.assemble.doLast {
                        project.copy { spec ->
                            spec.from(variant.mappingFile)
                            spec.into("${project.rootDir}/outputs")
                        }
                    }
                }
            }
        }

        /**
         * Defines standard signing configs for debugging and release.
         * Keystores must be located in keystore directory in project's root directory.
         */
        android.signingConfigs { container ->
            val keystoreDir = File(project.rootDir, "keystore")

            container.getByName("release") {
                it.keyAlias = keystoreProperties["key_alias"] as String
                it.storeFile = File(keystoreDir, keystoreProperties["key_file"] as String)
                it.storePassword = keystoreProperties["key_password"] as String
                it.keyPassword = keystoreProperties["key_password"] as String
            }

            container.getByName("debug") {
                it.keyAlias = "androiddebugkey"
                it.storeFile = File(keystoreDir, "debug.keystore")
                it.storePassword = "android"
                it.keyPassword = "android"
            }
        }

        /**
         * Defines standard build types: Debug, Beta and Release.
         * **Debug** type should be used only during development
         * **Beta** is used for internal testing
         * **Release** is used in production
         */
        android.buildTypes { container ->
            container.getByName("debug") {
                it.applicationIdSuffix = ".debug"
                it.manifestPlaceholders = mapOf(
                        "appId" to appProperties["package_name"] as String + it.applicationIdSuffix,
                        "appNameSuffix" to " D"
                )
            }

            container.getByName("beta") {
                it.applicationIdSuffix = ".beta"
                it.manifestPlaceholders = mapOf(
                        "appId" to appProperties["package_name"] as String + it.applicationIdSuffix,
                        "appNameSuffix" to " B " + android.defaultConfig.versionCode
                )

                it.signingConfig = android.signingConfigs.getByName("debug")
                it.isMinifyEnabled = true
                it.proguardFiles(android.getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            }

            container.getByName("release") {
                it.signingConfig = android.signingConfigs.getByName("release")
                it.isMinifyEnabled = true
                it.proguardFiles(android.getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
    }
}
