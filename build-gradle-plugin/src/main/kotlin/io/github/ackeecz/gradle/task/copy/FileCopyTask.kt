package io.github.ackeecz.gradle.task.copy

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Copying a file is not worth caching")
abstract class FileCopyTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
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
