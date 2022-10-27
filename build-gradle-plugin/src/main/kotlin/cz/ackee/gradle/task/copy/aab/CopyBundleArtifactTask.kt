package cz.ackee.gradle.task.copy.aab

import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import java.io.File

object CopyBundleArtifactTask {

    private const val taskName = "CopyBundle"
    private const val targetFileName = "App.aab"

    fun registerTask(
        project: Project,
        variant: Variant,
        getBundleArtifactTaskProvider: TaskProvider<GetBundleArtifactTask>,
        output: File,
    ): TaskProvider<Copy> {
        return project.tasks.register(createTaskName(variant), Copy::class.java) {
            dependsOn(getBundleArtifactTaskProvider.name)

            val bundleFileLocation = getBundleArtifactTaskProvider.get()
                .aabOutputFilePath.get()
                .asFile.readText()

            val bundleFile = File(bundleFileLocation)

            from(bundleFile)
            into(output)
            rename(bundleFile.name, targetFileName)
            group = Groups.WIP
        }
    }

    private fun createTaskName(variant: Variant) = "${variant.name}$taskName"
}
