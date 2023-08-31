package cz.ackee.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * App Distribution expects changelog in file `outputs/changelog.txt` where CI store the changelog. If this file does
 * not exist upload fails. This task ensures that the file exists
 */
abstract class CheckChangelogFileTask : DefaultTask() {

    @TaskAction
    fun onTaskExecution() {
        val file = File("${project.rootDir}/outputs/changelog.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    companion object {

        private const val taskName = "checkChangelogFileTask"

        fun registerTask(project: Project): TaskProvider<CheckChangelogFileTask> {
            return project.tasks.register<CheckChangelogFileTask>(createTaskName()) {
                group = Groups.DEPLOYMENT
            }
        }

        private fun createTaskName() = taskName
    }
}
