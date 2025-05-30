package io.github.ackeecz.gradle.plugin

import com.android.build.api.variant.ApplicationVariant
import io.github.ackeecz.gradle.task.CheckChangelogFileTask
import io.github.ackeecz.gradle.task.copy.aab.CopyBundleArtifactTask
import io.github.ackeecz.gradle.task.copy.apk.CopyApkArtifactTask
import io.github.ackeecz.gradle.task.copy.mapping.CopyMappingFileTask
import io.github.ackeecz.gradle.util.assembleTask
import io.github.ackeecz.gradle.util.bundleTask
import io.github.ackeecz.gradle.util.getApplicationAndroidComponents
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
        val copyApkArtifactTaskProvider = CopyApkArtifactTask.registerTask(project, this, outputs)
        assembleTask(project) { it.finalizedBy(copyApkArtifactTaskProvider) }
    }

    private fun ApplicationVariant.setUpCopyBundle(project: Project, outputs: File) {
        val copyBundleArtifactTaskProvider =
            CopyBundleArtifactTask.registerTask(project, this, outputs)
        bundleTask(project) { it.finalizedBy(copyBundleArtifactTaskProvider) }
    }

    private fun ApplicationVariant.setUpCopyMappingFile(project: Project, outputs: File) {
        val copyMappingFileTaskProvider = CopyMappingFileTask.registerTask(project, this, outputs)
        assembleTask(project) { it.finalizedBy(copyMappingFileTaskProvider) }
        bundleTask(project) { it.finalizedBy(copyMappingFileTaskProvider) }
    }

    private fun Project.setUpCheckingChangelog() {
        val changelogTaskProvider = CheckChangelogFileTask.registerTask(this@setUpCheckingChangelog)
        project.tasks.matching { it.name.startsWith("appDistributionUpload") }
            .configureEach { dependsOn(changelogTaskProvider) }
    }
}
