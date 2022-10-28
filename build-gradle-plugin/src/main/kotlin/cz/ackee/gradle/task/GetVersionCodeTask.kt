package cz.ackee.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

abstract class GetVersionCodeTask : DefaultTask() {

    private val logger = LoggerFactory.getLogger("get-version-code-task")

    @get:OutputFile
    abstract val versionCodeFile: RegularFileProperty

    @TaskAction
    fun onTaskExecution() {
        val versionCode = getCIEnvVersionCode() ?: getGitVersionCode()
        versionCodeFile.get().asFile.writeText(versionCode.toString())
    }

    private fun getCIEnvVersionCode(): Int? {
        return if (System.getenv(CI_ENV_VERSION_CODE) != null) {
            try {
                System.getenv(CI_ENV_VERSION_CODE).trim().toInt()
            } catch (e: Exception) {
                logger.warn("Exception while parsing CI_VERSION_CODE", e)
                null
            }
        } else {
            null
        }
    }

    private fun getGitVersionCode(): Int {
        val outputStream = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-list", "HEAD", "--count")
            standardOutput = outputStream
        }
        return outputStream.toString().trim().toInt()
    }

    companion object {

        private const val taskName = "getVersionCode"
        private const val CI_ENV_VERSION_CODE = "CI_VERSION_CODE"

        fun registerTask(project: Project): TaskProvider<GetVersionCodeTask> {
            val outputFile = project.layout.buildDirectory.file("git-version-code").get().asFile
            return project.tasks.register<GetVersionCodeTask>(taskName) {
                versionCodeFile.set(outputFile)
                outputs.upToDateWhen { false }
            }
        }
    }
}
