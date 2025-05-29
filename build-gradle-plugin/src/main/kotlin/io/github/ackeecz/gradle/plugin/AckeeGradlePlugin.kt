package io.github.ackeecz.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class AckeeGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project.plugins) {
            apply(ConfigureAppPlugin::class.java)
            apply(VariantsPlugin::class.java)
            apply(VerificationsPlugin::class.java)
            apply(DeploymentPlugin::class.java)
        }
    }
}
