package cz.ackee.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.net.URL
import java.util.Properties

private val logger = LoggerFactory.getLogger("ackee-gradle-plugin")

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
        val keystorePropertiesExt = project.extensions.create(
            "keystoreProperties",
            PropertiesExtensionKotlin::class.java,
            project,
            "keystore.properties"
        )
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

            android.lintOptions.apply {
                isCheckAllWarnings = true
                isWarningsAsErrors = true
                isAbortOnError = true
                isCheckDependencies = true
            }

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
                        copyAndRenameAPKTask.dependsOn(variant.assembleProvider)
                        copyAndRenameAPKTask.mustRunAfter(variant.assembleProvider)

                        // if assemble needs to automatically execute copyAndRenameAPKTask after
                        variant.assembleProvider.get().finalizedBy(copyAndRenameAPKTask)
                    }

                    // copy instrumentation APK used for testing to outputs/App-test.apk
                    variant.testVariant?.let { testVariant ->
                        val testApkFile = File(outputs, "App-test.apk")
                        testVariant.outputs.forEach { output ->
                            val testTaskName = "copyAndRename${variant.name.capitalize()}TestAPK"
                            val copyAndRenameTestAPKTask = tasks.create(testTaskName, Copy::class.java) {
                                from(output.outputFile.parent)
                                into(outputs)
                                include(output.outputFile.name)
                                rename(output.outputFile.name, testApkFile.name)
                            }

                            // if copyAndRenameTestAPKTask needs to automatically execute assemble before
                            copyAndRenameTestAPKTask.dependsOn(testVariant.assembleProvider)
                            copyAndRenameTestAPKTask.mustRunAfter(testVariant.assembleProvider)

                            // if assemble needs to automatically execute copyAndRenameTestAPKTask after
                            testVariant.assembleProvider.get().finalizedBy(copyAndRenameTestAPKTask)
                        }
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
                        val flavorName = if (variant.flavorName.isNullOrEmpty()) "" else "-${variant.flavorName}"
                        val aabName = "app$flavorName-${variant.buildType.name}.aab"

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
                        assembleProvider.get().doLast {
                            project.copy {
                                from(mappingFileProvider.get())
                                into(outputs)
                            }
                        }
                    }
                }

                /**
                 * Run "lint$BuildVariant" task before every "assemble$BuildVariant" tasks
                 */
                android.applicationVariants.forEach { variant ->
                    if (variant.buildType.isMinifyEnabled) {
                        variant.assembleProvider.get().dependsOn(tasks.named("lint${variant.name.capitalize()}"))
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
                            val file = File("${project.rootDir}/outputs/changelog.txt")
                            if (!file.exists()) {
                                file.createNewFile()
                            }
                        }
                    }

                    dependsOn(renameTaskName)
                }
            }

            project.tasks.create("copyGitHooks", type = Copy::class) {
                from("${project.rootDir}/.githooks")
                into("${project.rootDir}/.git/hooks")
            }

            /**
             * Before each assemble task of all build variants `copyGitHooks` task
             * is registered. We need to copy git hooks from `.githooks` folder to .git/hooks because its the
             * most universal cross-platform/cross-gitclients way how to achieve that everyone will use this hook.
             * There is an assumption that everyone who will clone this repository will at least once run
             * the app and then copy hooks so they will be applied to all developers.
             */
            project.tasks.whenTaskAdded {
                android.applicationVariants.forEach { variant ->
                    variant.outputs.all {
                        variant.assembleProvider.get().dependsOn(project.tasks["copyGitHooks"])
                    }
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

            android.defaultConfig.versionCode = resolveVersionCode(project)

            /**
             * Defines standard build types: Debug, Beta and Release.
             * **Debug** type should be used only during development
             * **Beta** is used for internal testing
             * **Release** is used in production
             */
            android.buildTypes {
                maybeCreate("debug").apply {
                    applicationIdSuffix = ".debug"
                    manifestPlaceholders += mapOf(
                        "appNameSuffix" to " D"
                    )
                }

                maybeCreate("beta").apply {
                    applicationIdSuffix = ".beta"
                    manifestPlaceholders += mapOf(
                        "appNameSuffix" to " B " + android.defaultConfig.versionCode
                    )

                    signingConfig = android.signingConfigs.getByName("debug")
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(android.getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }

                maybeCreate("release").apply {
                    signingConfig = android.signingConfigs.getByName("release")
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(android.getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
        }

        project.parent?.let {
            setupCodeCoverageTasks(it)
        }

        project.tasks.create("fetchDetektConfig") {
            group = "Reporting"
            description = "Fetch newest detekt config from master branch of styleguide Ackee GitHub repo"

            doLast {
                val configText = URL("https://raw.githubusercontent.com/AckeeCZ/styleguide/master/android/detekt-config.yml").readText()
                val configFile = File(project.rootDir, "detekt-config-common.yml")
                configFile.writeText(configText)
            }
        }
    }

    /**
     * Resolve app version code. If `CI_VERSION_CODE` environment variable is defined, try to parse that.
     * Otherwise use git commits count.
     */
    private fun resolveVersionCode(project: Project): Int {
        return if (System.getenv("CI_VERSION_CODE") != null) {
            try {
                Integer.parseInt(System.getenv("CI_VERSION_CODE").trim())
            } catch (e: Exception) {
                logger.warn("Exception while parsing CI_VERSION_CODE", e)
                getGitCommitsCount(project)
            }
        } else {
            getGitCommitsCount(project)
        }
    }
}
