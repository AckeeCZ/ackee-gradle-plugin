package cz.ackee.gradle.task.copy.aab

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register

abstract class GetBundleArtifactTask : DefaultTask() {

    @get:InputFiles
    abstract val aabInputFile: RegularFileProperty

    @get:OutputFile
    abstract val aabOutputFile: RegularFileProperty

    @get:OutputFile
    abstract val aabOutputFilePath: RegularFileProperty

    @TaskAction
    fun onTaskExecution() {
        val artifact = aabOutputFile.get().asFile
        aabOutputFilePath.get().asFile.writeText(artifact.absolutePath)

        aabInputFile.get().asFile.copyTo(aabOutputFile.get().asFile)
    }

    companion object {

        private const val taskName = "getBundleArtifact"

        fun registerTask(project: Project, variant: Variant): TaskProvider<GetBundleArtifactTask> {
            return project.tasks.register<GetBundleArtifactTask>(createTaskName(variant)) {
                val outputFile = project.layout.buildDirectory.file("aab-location")
                aabOutputFilePath.set(outputFile)
                group = Groups.DEPLOYMENT
            }.also {
                variant.artifacts.use(it)
                    .wiredWithFiles(GetBundleArtifactTask::aabInputFile, GetBundleArtifactTask::aabOutputFile)
                    .toTransform(SingleArtifact.BUNDLE)
            }
        }

        private fun createTaskName(variant: Variant) = "$taskName${variant.name.capitalized()}"
    }
}
