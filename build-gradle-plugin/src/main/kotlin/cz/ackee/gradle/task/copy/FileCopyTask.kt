package cz.ackee.gradle.task.copy

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class FileCopyTask : DefaultTask() {

    @get:InputFile
    abstract val from: RegularFileProperty

    @get:OutputFile
    abstract val to: RegularFileProperty

    @TaskAction
    fun onTaskExecute() {
        val toFile = to.get().asFile
        if (toFile.exists()) {
            toFile.delete()
        }

        from.get().asFile.copyTo(toFile)
    }
}
