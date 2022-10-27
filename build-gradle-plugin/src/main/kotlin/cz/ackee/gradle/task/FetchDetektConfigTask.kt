package cz.ackee.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.net.URL

abstract class FetchDetektConfigTask : DefaultTask() {

    @get:Input
    abstract val url: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun onTaskExecution() {
        val configText = URL(url.get()).readText()
        outputFile.get().asFile.writeText(configText)
    }

    companion object {

        // TODO rename
        private const val taskName = "fetchDetektConfig2"

        fun registerTask(project: Project, url: String, outputFile: File): TaskProvider<FetchDetektConfigTask> {
            return project.tasks.register(taskName, FetchDetektConfigTask::class.java) {
                this.url.set(url)
                this.outputFile.set(outputFile)
                group = Groups.REPORTING
            }
        }
    }
}
