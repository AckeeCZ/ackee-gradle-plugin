package cz.ackee.gradle.task.copy.mapping

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
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
            if (outputFile.exists()) {
                outputFile.delete()
            }

            inputFile.copyTo(outputFile)
        }
    }

    companion object {

        private const val taskName = "copyMappingFile"

        fun registerTask(
            project: Project,
            variant: Variant,
            output: File,
        ): TaskProvider<CopyMappingFileTask> {
            return project.tasks.register<CopyMappingFileTask>(createTaskName(variant)) {
                inputMappingFile.set(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE))
                outputMappingFile.set(output)
                group = Groups.WIP
            }
        }

        private fun createTaskName(variant: Variant) = "$taskName${variant.name.capitalized()}"
    }
}
