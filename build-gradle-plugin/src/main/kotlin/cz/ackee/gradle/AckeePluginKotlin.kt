package cz.ackee.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.util.Properties

import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.extra

class AckeePluginKotlin : Plugin<Project> {

    companion object {

        /**
         * Returns number of commits in the current branch of the project.
         */
        fun getGitCommitsCount(project: Project): Int {
            val stdout = ByteArrayOutputStream()
            project.exec {
                commandLine = listOf("git", "rev-list", "HEAD", "--count")
                standardOutput = stdout
            }
            return Integer.parseInt(stdout.toString().trim())
        }
    }

    override fun apply(project: Project) {

        /**
         * Define properties with keystore info
         */
        val keystorePropertiesExt = project.extensions.create("keystoreProperties", PropertiesExtensionKotlin::class.java, project, "keystore.properties")
        val keystoreProperties by project.extra(Properties().apply {
            load(BufferedReader(FileReader(project.file(keystorePropertiesExt.fullPath))))
        })

        /**
         * Define properties with application info
         */
        val appPropertiesExt = project.extensions.create("appProperties", PropertiesExtensionKotlin::class.java, project, "app.properties")
        val appProperties by project.extra(Properties().apply {
            load(BufferedReader(FileReader(project.file(appPropertiesExt.fullPath))))
        })

        /**
         * Number of git commits.
         * Also available outside of the plugin scope using [extra].
         */
        val gitCommitsCount: Int by project.extra(getGitCommitsCount(project))

        /**
         * Configure the android plugin once it is applied.
         */
        project.pluginManager.withPlugin("com.android.application") {
            val android = project.extensions.findByType(AppExtension::class.java) ?: throw Exception(
                    "Not an Android application. Did you forget to apply 'com.android.application' plugin?"
            )

            project.afterEvaluate {

                val outputs = File(project.rootDir, "outputs")
                outputs.mkdir()

                /**
                 * Set output apk destination to file App.apk in outputs folder in project root
                 */
                android.applicationVariants.forEach { variant ->

                    val apkFile = File(outputs, "App.apk")

                    variant.outputs.forEach { output ->
                        val taskName = "copyAndRename${variant.name.capitalize()}APK"
                        val copyAndRenameAPKTask = tasks.create(taskName, Copy::class.java) {
                            from(output.outputFile.parent)
                            into(outputs)
                            include(output.outputFile.name)
                            rename(output.outputFile.name, apkFile.name)
                        }

                        // if copyAndRenameAPKTask needs to automatically execute assemble before
                        copyAndRenameAPKTask.dependsOn(variant.assemble)
                        copyAndRenameAPKTask.mustRunAfter(variant.assemble)

                        // if assemble needs to automatically execute copyAndRenameAPKTask after
                        variant.assemble.finalizedBy(copyAndRenameAPKTask)
                    }
                }

                /**
                 * Create dynamic task for each of `bundleXXX` tasks that will be performed after creation of
                 * app bundle is done. This task will copy generated aab file to `outputs/App.aab` file where
                 * CI server expects it.
                 */
                android.applicationVariants.forEach { variant ->

                    val aabFile = File(outputs, "App.aab")

                    val taskName = "copyAndRename${variant.name.capitalize()}Aab"
                    val copyAndRenameAABTask = tasks.create(taskName, Copy::class.java) {
                        val path = "${buildDir}/outputs/bundle/${variant.name}/"
                        val aabName = "app-${variant.flavorName}-${variant.buildType.name}.aab"

                        from(path)
                        into(outputs)
                        include(aabName)
                        rename(aabName, aabFile.name)
                    }

                    val bundleTask = tasks.named("bundle${variant.name.capitalize()}").get()
                    // if copyAndRenameAABTask needs to automatically execute assemble before
                    copyAndRenameAABTask.dependsOn(bundleTask)
                    copyAndRenameAABTask.mustRunAfter(bundleTask)

                    // if assemble needs to automatically execute copyAndRenameAABTask after
                    bundleTask.finalizedBy(copyAndRenameAABTask)
                }

                /**
                 * Copy mapping.txt from its location to outputs folder in project root
                 */
                android.applicationVariants.all {
                    if (buildType.isMinifyEnabled) {
                        assemble.doLast {
                            project.copy {
                                from(mappingFile)
                                into(outputs)
                            }
                        }
                    }
                }
            }

            /**
             * App Distribution expects changelog in file `outputs/changelog.txt` where Jenkins store the changelog. If this file does
             * not exist upload fails. This task ensures that the file exists
             */
            project.tasks.whenTaskAdded {
                if (name.startsWith("appDistributionUpload")) {
                    val renameTaskName = "checkChangelogFileTask${name.capitalize()}"
                    project.tasks.create(renameTaskName) {
                        doLast {
                            println("In changelog creation")
                            val file = File("${project.rootDir}/outputs/changelog.txt")
                            if (!file.exists()) {
                                file.createNewFile()
                            }
                        }
                    }

                    dependsOn(renameTaskName)
                }
            }

            /**
             * Defines standard signing configs for debugging and release.
             * Keystores must be located in keystore directory in project's root directory.
             */
            android.signingConfigs {
                val keystoreDir = File(project.rootDir, "keystore")

                maybeCreate("release").apply {
                    keyAlias = keystoreProperties["key_alias"] as String?
                    storeFile = File(keystoreDir, keystoreProperties["key_file"] as String?)
                    storePassword = keystoreProperties["key_password"] as String?
                    keyPassword = keystoreProperties["key_password"] as String?
                }

                maybeCreate("debug").apply {
                    keyAlias = "androiddebugkey"
                    storeFile = File(keystoreDir, "debug.keystore")
                    storePassword = "android"
                    keyPassword = "android"
                }
            }

            /**
             * Set default version code to git commits count.
             * User still can set their own value though.
             */
            android.defaultConfig.versionCode = gitCommitsCount

            /**
             * Defines standard build types: Debug, Beta and Release.
             * **Debug** type should be used only during development
             * **Beta** is used for internal testing
             * **Release** is used in production
             */
            android.buildTypes {
                maybeCreate("debug").apply {
                    applicationIdSuffix = ".debug"
                    manifestPlaceholders = mapOf(
                            "appNameSuffix" to " D"
                    )
                }

                maybeCreate("beta").apply {
                    applicationIdSuffix = ".beta"
                    manifestPlaceholders = mapOf(
                            "appNameSuffix" to " B " + android.defaultConfig.versionCode
                    )

                    signingConfig = android.signingConfigs.getByName("debug")
                    isMinifyEnabled = true
                    proguardFiles(android.getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
                }

                maybeCreate("release").apply {
                    signingConfig = android.signingConfigs.getByName("release")
                    isMinifyEnabled = true
                    proguardFiles(android.getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
        }
    }
}
