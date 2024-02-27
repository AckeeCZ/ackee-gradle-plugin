package cz.ackee.gradle.plugin

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import java.util.Locale

/**
 * Configures app with dependencies check logic like checking available updates of dependencies or
 * vulnerabilities. This plugin needs to be applied to the root Gradle [Project] only!
 */
class DependenciesCheckPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        DependenciesUpdateCheckConfigurator.configure(target)
        DependenciesVulnerabilitiesCheckConfigurator.configure(target)
    }
}

private object DependenciesUpdateCheckConfigurator {

    fun configure(project: Project) {
        project.configureInternal()
    }

    private fun Project.configureInternal() {
        pluginManager.apply("com.github.ben-manes.versions")

        tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
            outputDir = "build/reports"
            // Danger relies on this format and file name to parse the results
            outputFormatter = "xml"
            reportfileName = "dependency-updates-report"

            // Configure when the candidate version is rejected and not suggested as update
            rejectVersionIf {
                // We reject the new version and don't suggest update if current version is stable, but the
                // candidate is not, since we don't want to update from stable to unstable versions
                isNonStable(candidate.version) && !isNonStable(currentVersion)
            }
        }
    }

    private fun isNonStable(version: String): Boolean {
        // If version contains any of those keywords, we consider it to be stable
        val containsStableKeyword = listOf("RELEASE", "FINAL", "GA").any {
            version.uppercase(Locale.getDefault()).contains(it)
        }
        // To be stable, version has to consist of numbers, dots, commas and possibly letter v,
        // version can have letter r in the end
        val stableRegex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = containsStableKeyword || stableRegex.matches(version)
        return isStable.not()
    }
}

private object DependenciesVulnerabilitiesCheckConfigurator {

    fun configure(project: Project) {
        project.allprojects {
            pluginManager.apply("org.owasp.dependencycheck")

            extensions.configure(DependencyCheckExtension::class) {
                // Danger relies on this format to parse the results
                format = "XML"

                // Change path to database data only for CI builds to be able to cache it between
                // pipelines (Gitlab CI can cache data located only in the project folder) and do
                // not change it locally to have only one DB for all local projects
                if (System.getenv("CI_PIPELINE_SOURCE") != null) {
                    // CI relies on this path to cache the fetched database properly
                    data.directory = "${project.rootDir.absolutePath}/.gradle/dependency-check-data"
                }

                cveValidForHours = 24
                hostedSuppressions.validForHours = 24
            }
        }
    }
}
