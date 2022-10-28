package cz.ackee.gradle.task.copy.aab

import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
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
    ): TaskProvider<Copy> {
        return project.tasks.register<Copy>(createTaskName(variant)) {
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

    private fun createTaskName(variant: Variant) = "$taskName${variant.name.capitalized()}"
}
