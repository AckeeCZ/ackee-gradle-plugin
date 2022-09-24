package cz.ackee.gradle.plugin

import com.android.build.gradle.AppExtension
import cz.ackee.gradle.getAndroidAppExtensionOrThrow
import cz.ackee.gradle.setupCodeCoverageTasks
import cz.ackee.gradle.task.FetchDetektConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class VerificationsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.setupDetekt()
        project.setupCodeCoverage()

        val android = project.getAndroidAppExtensionOrThrow()
        android.setupLint()
    }

    private fun Project.setupDetekt() {
        val configFile = File(rootDir, "detekt-config-common.yml")
        FetchDetektConfigTask.registerTask(this, DETEKT_CONFIG_URL, configFile)
    }

    private fun Project.setupCodeCoverage() {
        project.parent?.let(::setupCodeCoverageTasks)
    }

    // TODO resolve this deprecation
    private fun AppExtension.setupLint() {
        lintOptions.apply {
            isCheckAllWarnings = true
            isWarningsAsErrors = true
            isAbortOnError = true
            isCheckDependencies = true
        }
    }

    companion object {

        // TODO move this to Extension?
        private const val DETEKT_CONFIG_URL = "https://raw.githubusercontent.com/AckeeCZ/styleguide/master/android/detekt-config.yml"
    }
}
