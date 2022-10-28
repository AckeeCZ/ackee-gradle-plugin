package cz.ackee.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File

class CheckChangelogFileTask : DefaultTask() {

    @TaskAction
    fun onTaskExecution() {
        val file = File("${project.rootDir}/outputs/changelog.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    companion object {

        private const val taskName = "checkChangelogFileTask"

        fun registerTask(project: Project, appDistributionUploadTask: Task) {
            project.tasks.register<CheckChangelogFileTask>(createTaskName(appDistributionUploadTask)) {
                appDistributionUploadTask.dependsOn(this)
            }
        }

        private fun createTaskName(appDistributionUploadTask: Task) = "$taskName${appDistributionUploadTask.name}"
    }
}
