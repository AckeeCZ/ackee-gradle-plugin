package cz.ackee.gradle.task.copy.apk

import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import java.io.File

object CopyApkArtifactTask {

    private const val taskName = "CopyApk"
    private const val targetFileName = "App.apk"

    fun registerTask(
        project: Project,
        variant: Variant,
        getApkArtifactTaskProvider: TaskProvider<GetApkArtifactTask>,
        output: File,
    ): TaskProvider<Copy> {
        return project.tasks.register(createTaskName(variant), Copy::class.java) {
            dependsOn(getApkArtifactTaskProvider.name)

            val apkFileLocation = getApkArtifactTaskProvider.get()
                .apkOutputFilePath.get()
                .asFile.readText()
            val apkFile = File(apkFileLocation)

            from(apkFile)
            into(output)
            rename(apkFile.name, targetFileName)
            group = Groups.WIP
        }
    }

    private fun createTaskName(variant: Variant) = "${variant.name}$taskName"
}
