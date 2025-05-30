package io.github.ackeecz.gradle.task.copy.apk

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.Variant
import io.github.ackeecz.gradle.task.Groups
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

abstract class CopyApkArtifactTask : DefaultTask() {

    @get:InputFiles
    abstract val apkFolder: DirectoryProperty

    @get:OutputFile
    abstract val outputApkFile: RegularFileProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun onTaskExecution() {
        val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
            ?: throw RuntimeException("Cannot load APKs")

        builtArtifacts.elements.first()
            .outputFile
            .let(::File)
            .copyTo(outputApkFile.asFile.get(), overwrite = true)
    }

    companion object {

        private const val TASK_NAME = "copyApk"
        private const val TARGET_FILE_NAME = "App.apk"

        fun registerTask(
            project: Project,
            variant: Variant,
            output: File,
        ): TaskProvider<CopyApkArtifactTask> {
            return project.tasks.register<CopyApkArtifactTask>(createTaskName(variant)) {
                apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
                builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                outputApkFile.set(File(output, TARGET_FILE_NAME))
                group = Groups.DEPLOYMENT
            }
        }

        private fun createTaskName(variant: Variant) = "$TASK_NAME${variant.name.uppercaseFirstChar()}"
    }
}
