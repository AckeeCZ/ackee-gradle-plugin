package cz.ackee.gradle.plugin

import com.android.build.api.dsl.Lint
import cz.ackee.gradle.util.getApplicationAndroidComponents
import cz.ackee.gradle.setUpCodeCoverageTasks
import cz.ackee.gradle.task.FetchDetektConfigTask
import cz.ackee.gradle.task.copy.githooks.CopyGitHooksTask
import cz.ackee.gradle.util.assembleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class VerificationsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            setUpDetekt()
            setUpCodeCoverage()
            setUpLint()
            setUpCopyGitHooks()
        }
    }

    private fun Project.setUpDetekt() {
        val configFile = File(rootDir, "detekt-config-common.yml")
        FetchDetektConfigTask.registerTask(this, DETEKT_CONFIG_URL, configFile)
    }

    private fun Project.setUpCodeCoverage() {
        project.parent?.let(::setUpCodeCoverageTasks)
    }

    private fun Project.setUpLint() {
        val androidComponents = project.getApplicationAndroidComponents()
        androidComponents.finalizeDsl {
            it.lint { setUp() }
        }
    }

    private fun Lint.setUp() {
        checkAllWarnings = true
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
    }

    private fun Project.setUpCopyGitHooks() {
        val copyGitHooksProvider = CopyGitHooksTask.registerTask(project)
        project.getApplicationAndroidComponents().onVariants { variant ->
            variant.assembleTask(project) { it.dependsOn(copyGitHooksProvider) }
        }
    }

    companion object {

        private const val DETEKT_CONFIG_URL = "https://raw.githubusercontent.com/AckeeCZ/styleguide/master/android/detekt-config.yml"
    }
}
