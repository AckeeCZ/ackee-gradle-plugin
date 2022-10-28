package cz.ackee.gradle.plugin

import cz.ackee.gradle.getApplicationAndroidComponents
import cz.ackee.gradle.task.copy.aab.CopyBundleArtifactTask
import cz.ackee.gradle.task.copy.aab.GetBundleArtifactTask
import cz.ackee.gradle.task.copy.apk.CopyApkArtifactTask
import cz.ackee.gradle.task.copy.apk.GetApkArtifactTask
import cz.ackee.gradle.task.copy.mapping.CopyMappingFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class CopyArtifactsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents = project.getApplicationAndroidComponents()
        val outputs = File(project.rootDir, "outputs")

        androidComponents.onVariants { variant ->
            val getApkArtifactTaskProvider = GetApkArtifactTask.registerTask(project, variant)
            val copyApkArtifactTaskProvider = CopyApkArtifactTask.registerTask(project, variant, getApkArtifactTaskProvider, outputs)
            getApkArtifactTaskProvider.configure { finalizedBy(copyApkArtifactTaskProvider) }

            val getBundleArtifactTask = GetBundleArtifactTask.registerTask(project, variant)
            val copyBundleArtifactTask = CopyBundleArtifactTask.registerTask(project, variant, getBundleArtifactTask, outputs)
            getBundleArtifactTask.configure { finalizedBy(copyBundleArtifactTask) }

            // TODO chain this to task graph somehow
            CopyMappingFileTask.registerTask(project, variant, outputs)
        }
    }
}
