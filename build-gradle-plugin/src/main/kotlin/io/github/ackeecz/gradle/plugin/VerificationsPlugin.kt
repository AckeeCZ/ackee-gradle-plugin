package io.github.ackeecz.gradle.plugin

import com.android.build.api.dsl.Lint
import io.github.ackeecz.gradle.setUpCodeCoverageTasks
import io.github.ackeecz.gradle.task.FetchDetektConfigTask
import io.github.ackeecz.gradle.task.copy.githooks.CopyGitHooksTask
import io.github.ackeecz.gradle.util.assembleTask
import io.github.ackeecz.gradle.util.getApplicationAndroidComponents
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
        val copyGitHooksProvider = CopyGitHooksTask.registerTask(project) ?: return
        project.getApplicationAndroidComponents().onVariants { variant ->
            variant.assembleTask(project) { it.dependsOn(copyGitHooksProvider) }
        }
    }

    companion object {

        private const val DETEKT_CONFIG_URL = "https://raw.githubusercontent.com/AckeeCZ/styleguide/master/android/detekt-config.yml"
    }
}
