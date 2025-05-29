package io.github.ackeecz.gradle.task.copy.mapping

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import io.github.ackeecz.gradle.task.Groups
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

abstract class CopyMappingFileTask : DefaultTask() {

    @get:InputFile
    @get:Optional
    abstract val inputMappingFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputMappingFile: DirectoryProperty

    @TaskAction
    fun onTaskExecution() {
        if (inputMappingFile.isPresent) {
            val inputFile = inputMappingFile.get().asFile
            val outputFile = File(outputMappingFile.get().asFile, inputFile.name)
            inputFile.copyTo(outputFile, overwrite = true)
        }
    }

    companion object {

        private const val TASK_NAME = "copyMappingFile"

        fun registerTask(
            project: Project,
            variant: Variant,
            output: File,
        ): TaskProvider<CopyMappingFileTask> {
            return project.tasks.register<CopyMappingFileTask>(createTaskName(variant)) {
                inputMappingFile.set(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE))
                outputMappingFile.set(output)
                group = Groups.DEPLOYMENT
            }
        }

        private fun createTaskName(variant: Variant) = "$TASK_NAME${variant.name.uppercaseFirstChar()}"
    }
}
