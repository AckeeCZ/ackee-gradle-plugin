package cz.ackee.gradle.task.copy.apk

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register

abstract class GetApkArtifactTask : DefaultTask() {

    @get:InputFiles
    abstract val apkInputFolder: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:OutputDirectory
    abstract val apkOutputFolder: DirectoryProperty

    @get:OutputFile
    abstract val apkOutputFilePath: RegularFileProperty

    @TaskAction
    fun onTaskExecution() {
        val builtArtifacts = builtArtifactsLoader.get().load(apkInputFolder.get()) ?: throw RuntimeException("Cannot load artifacts")
        val artifact = builtArtifacts.elements.first()
        apkOutputFilePath.get().asFile.writeText(artifact.outputFile)
    }

    companion object {

        private const val taskName = "getApkArtifact"

        fun registerTask(project: Project, variant: Variant): TaskProvider<GetApkArtifactTask> {
            return project.tasks.register<GetApkArtifactTask>(createTaskName(variant)) {
                builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                apkOutputFilePath.set(project.layout.buildDirectory.file("apk-location"))
                group = Groups.WIP
            }.also {
                variant.artifacts.use(it)
                    .wiredWithDirectories(GetApkArtifactTask::apkInputFolder, GetApkArtifactTask::apkOutputFolder)
                    .toTransform(SingleArtifact.APK)
            }
        }

        private fun createTaskName(variant: Variant) = "$taskName${variant.name.capitalized()}"
    }
}
