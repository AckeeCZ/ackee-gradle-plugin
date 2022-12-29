package cz.ackee.gradle.task.copy.aab

import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import cz.ackee.gradle.task.copy.FileCopyTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import java.io.File

object CopyBundleArtifactTask {

    private const val taskName = "copyBundle"
    private const val targetFileName = "App.aab"

    fun registerTask(
        project: Project,
        variant: Variant,
        getBundleArtifactTaskProvider: TaskProvider<GetBundleArtifactTask>,
        output: File,
    ): TaskProvider<FileCopyTask> {
        return project.tasks.register<FileCopyTask>(createTaskName(variant)) {
            val bundleFilePath = getBundleArtifactTaskProvider.get().aabOutputFilePath
            fromPath.set(bundleFilePath)
            to.set(File(output, targetFileName))
            group = Groups.DEPLOYMENT
        }
    }

    private fun createTaskName(variant: Variant) = "$taskName${variant.name.capitalized()}"
}
