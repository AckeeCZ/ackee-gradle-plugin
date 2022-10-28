package cz.ackee.gradle.plugin

import com.android.build.api.variant.ApplicationVariant
import cz.ackee.gradle.getApplicationAndroidComponents
import cz.ackee.gradle.task.CheckChangelogFileTask
import cz.ackee.gradle.task.copy.aab.CopyBundleArtifactTask
import cz.ackee.gradle.task.copy.aab.GetBundleArtifactTask
import cz.ackee.gradle.task.copy.apk.CopyApkArtifactTask
import cz.ackee.gradle.task.copy.apk.GetApkArtifactTask
import cz.ackee.gradle.task.copy.mapping.CopyMappingFileTask
import cz.ackee.gradle.util.assembleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class DeploymentPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            setUpArtifactsCopying()
            setUpCheckingChangelog()
        }
    }

    private fun Project.setUpArtifactsCopying() {
        val androidComponents = getApplicationAndroidComponents()
        val outputs = File(rootDir, "outputs")
        androidComponents.onVariants { variant ->
            with(variant) {
                setUpCopyApk(this@setUpArtifactsCopying, outputs)
                setUpCopyBundle(this@setUpArtifactsCopying, outputs)
                setUpCopyMappingFile(this@setUpArtifactsCopying, outputs)
            }
        }
    }

    private fun ApplicationVariant.setUpCopyApk(project: Project, outputs: File) {
        val getApkArtifactTaskProvider = GetApkArtifactTask.registerTask(project, this)
        val copyApkArtifactTaskProvider = CopyApkArtifactTask.registerTask(project, this, getApkArtifactTaskProvider, outputs)
        getApkArtifactTaskProvider.configure { finalizedBy(copyApkArtifactTaskProvider) }
    }

    private fun ApplicationVariant.setUpCopyBundle(project: Project, outputs: File) {
        val getBundleArtifactTaskProvider = GetBundleArtifactTask.registerTask(project, this)
        val copyBundleArtifactTaskProvider = CopyBundleArtifactTask.registerTask(project, this, getBundleArtifactTaskProvider, outputs)
        getBundleArtifactTaskProvider.configure { finalizedBy(copyBundleArtifactTaskProvider) }
    }

    private fun ApplicationVariant.setUpCopyMappingFile(project: Project, outputs: File) {
        val copyMappingFileTaskProvider = CopyMappingFileTask.registerTask(project, this, outputs)
        this.assembleTask(project) { it.finalizedBy(copyMappingFileTaskProvider) }
    }

    private fun Project.setUpCheckingChangelog() {
        tasks.whenTaskAdded {
            if (name.startsWith("appDistributionUpload")) {
                CheckChangelogFileTask.registerTask(this@setUpCheckingChangelog, this)
            }
        }
    }
}
