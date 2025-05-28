package io.github.ackeecz.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * App Distribution expects changelog in file `outputs/changelog.txt` where CI store the changelog. If this file does
 * not exist upload fails. This task ensures that the file exists
 */
abstract class CheckChangelogFileTask : DefaultTask() {

    @get:OutputFile
    abstract val changeLogFile: RegularFileProperty

    @TaskAction
    fun onTaskExecution() {
        val file = changeLogFile.asFile.get()
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    companion object {

        private const val taskName = "checkChangelogFileTask"

        fun registerTask(project: Project): TaskProvider<CheckChangelogFileTask> {
            return project.tasks.register<CheckChangelogFileTask>(createTaskName()) {
                changeLogFile.set(File("${project.rootDir}/outputs", "changelog.txt"))
                group = Groups.DEPLOYMENT
            }
        }

        private fun createTaskName() = taskName
    }
}
