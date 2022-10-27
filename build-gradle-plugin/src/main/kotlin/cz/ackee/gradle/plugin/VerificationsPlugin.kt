package cz.ackee.gradle.plugin

import com.android.build.api.dsl.Lint
import cz.ackee.gradle.getAndroidComponents
import cz.ackee.gradle.setupCodeCoverageTasks
import cz.ackee.gradle.task.FetchDetektConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class VerificationsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.setupDetekt()
        project.setupCodeCoverage()

        val androidComponents = project.getAndroidComponents()
        androidComponents.finalizeDsl {
            it.lint { setup() }
        }
    }

    private fun Project.setupDetekt() {
        val configFile = File(rootDir, "detekt-config-common.yml")
        FetchDetektConfigTask.registerTask(this, DETEKT_CONFIG_URL, configFile)
    }

    private fun Project.setupCodeCoverage() {
        project.parent?.let(::setupCodeCoverageTasks)
    }

    private fun Lint.setup() {
        checkAllWarnings = true
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
    }

    companion object {

        // TODO move this to Extension?
        private const val DETEKT_CONFIG_URL = "https://raw.githubusercontent.com/AckeeCZ/styleguide/master/android/detekt-config.yml"
    }
}
