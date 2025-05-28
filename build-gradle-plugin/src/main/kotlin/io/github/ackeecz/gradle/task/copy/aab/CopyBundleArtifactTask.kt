package io.github.ackeecz.gradle.task.copy.aab

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import io.github.ackeecz.gradle.task.Groups
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

abstract class CopyBundleArtifactTask : DefaultTask() {

    @get:InputFiles
    abstract val aabFileInput: RegularFileProperty

    @get:OutputFile
    abstract val aabFileOutput: RegularFileProperty

    @TaskAction
    fun onTaskExecution() {
        val aabFile = aabFileInput.get().asFile
        aabFile.copyTo(aabFileOutput.get().asFile, overwrite = true)
    }

    companion object {

        private const val TASK_NAME = "copyBundle"
        private const val TARGET_FILE_NAME = "App.aab"

        fun registerTask(
            project: Project,
            variant: Variant,
            output: File,
        ): TaskProvider<CopyBundleArtifactTask> {
            return project.tasks.register<CopyBundleArtifactTask>(createTaskName(variant)) {
                aabFileInput.set(variant.artifacts.get(SingleArtifact.BUNDLE))
                aabFileOutput.set(File(output, TARGET_FILE_NAME))
                group = Groups.DEPLOYMENT
            }
        }

        private fun createTaskName(variant: Variant) = "$TASK_NAME${variant.name.uppercaseFirstChar()}"
    }
}
