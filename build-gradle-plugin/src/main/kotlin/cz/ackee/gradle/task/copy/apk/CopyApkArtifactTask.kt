package cz.ackee.gradle.task.copy.apk

import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import cz.ackee.gradle.task.copy.FileCopyTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import java.io.File

object CopyApkArtifactTask {

    private const val taskName = "copyApk"
    private const val targetFileName = "App.apk"

    fun registerTask(
        project: Project,
        variant: Variant,
        getApkArtifactTaskProvider: TaskProvider<GetApkArtifactTask>,
        output: File,
    ): TaskProvider<FileCopyTask> {
        return project.tasks.register<FileCopyTask>(createTaskName(variant)) {
            val apkFilePath = getApkArtifactTaskProvider.get().apkOutputFilePath
            fromPath.set(apkFilePath)
            to.set(File(output, targetFileName))
            group = Groups.DEPLOYMENT
        }
    }

    private fun createTaskName(variant: Variant) = "$taskName${variant.name.capitalized()}"
}
