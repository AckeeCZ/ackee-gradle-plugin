package io.github.ackeecz.gradle.task.copy

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class FileCopyTask : DefaultTask() {

    @get:InputFile
    abstract val fromPath: RegularFileProperty

    @get:OutputFile
    abstract val to: RegularFileProperty

    @TaskAction
    fun onTaskExecute() {
        val toFile = to.get().asFile
        if (toFile.exists()) {
            toFile.delete()
        }

        val fromFile = fromPath.get()
            .asFile
            .readText()
            .let(::File)

        fromFile.copyTo(toFile)
    }
}
