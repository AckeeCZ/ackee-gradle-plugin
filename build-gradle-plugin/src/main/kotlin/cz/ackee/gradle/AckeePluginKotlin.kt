package cz.ackee.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
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
         * Define properties with application info
         */
        val appPropertiesExt = project.extensions.create("appProperties", PropertiesExtensionKotlin::class.java, project, "app.properties")
        val appProperties by project.extra(Properties().loadKeystoreProperties(project.file(appPropertiesExt.fullPath)))

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

            with(project) {

                val outputs = File(project.rootDir, "outputs")
                outputs.mkdir()

                /**
                 * Copy mapping.txt from its location to outputs folder in project root
                 */
                /*android.applicationVariants.configureEach {
                    if (buildType.isMinifyEnabled) {
                        assembleProvider.get().doLast {
                            project.copy {
                                from(mappingFileProvider.get())
                                into(outputs)
                            }
                        }
                    }
                }*/

                /**
                 * Run "lint$BuildVariant" task before every "assemble$BuildVariant" tasks
                 */
                android.applicationVariants.configureEach {
                    val variant = this
                    if (!variant.buildType.isDebuggable) {
                        variant.assembleProvider.dependsOn(tasks.named("lint${variant.name.capitalize()}"))
                    }
                }
            }

            /**
             * App Distribution expects changelog in file `outputs/changelog.txt` where CI store the changelog. If this file does
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

            android.applicationVariants.configureEach {
                val variant = this
                variant.outputs.configureEach {
                    variant.assembleProvider.get().dependsOn(project.tasks["copyGitHooks"])
                }
            }

            android.defaultConfig.versionCode = resolveVersionCode(project)
        }
    }

    private fun Properties.loadKeystoreProperties(keystorePropertiesFile: File): Properties {
        val fileReader = FileReader(keystorePropertiesFile)
        val bufferedReader = BufferedReader(fileReader)
        load(bufferedReader)
        return this
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
