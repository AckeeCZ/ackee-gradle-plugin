package cz.ackee.gradle

import cz.ackee.gradle.util.capitalizeFirstChar
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.extra
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * TODO refactor this a bit
 *
 * Setup JaCoCo code coverage tasks. First create a task for each submodule that computes
 * coverage for that module. Then define task that aggregates those results and prints full
 * report for the whole project.
 */
@Suppress("UNCHECKED_CAST")
fun setUpCodeCoverageTasks(project: Project) {
    val excludedProjects = (project.extra.getIfExists("jacocoExcludedProjects") as? List<String>) ?: emptyList()
    val testVariant = (project.extra.getIfExists("jacocoTestVariant") as? String) ?: "devApiDebug"
    val excludedFiles = (project.extra.getIfExists("jacocoExcludedFiles") as? List<String>) ?: emptyList()

    val coveredProjects = project.subprojects.filter { !excludedProjects.contains(it.name) }

    // generate tasks for each subproject
    coveredProjects.forEach {
        with(it) {
            plugins.apply("jacoco")
            extensions.configure(JacocoPluginExtension::class.java) {
                this.toolVersion = "0.8.8"
            }
            tasks.create("jacocoTestReport", JacocoReport::class.java) {
                group = "Reporting"
                description = "Generate Jacoco coverage"

                this.dependsOn("test${testVariant.capitalizeFirstChar()}UnitTest")
                reports {
                    csv.required.set(false)
                    xml.required.set(false)
                    html.required.set(true)
                }

                val kotlinTree = fileTree(mapOf("dir" to "${buildDir}/tmp/kotlin-classes/$testVariant", "excludes" to excludedFiles))
                val javacTree = fileTree(mapOf("dir" to "${buildDir}/intermediates/javac/$testVariant", "excludes" to excludedFiles))
                val mainJavaSrc = "${projectDir}/src/main/java"
                val mainKotlinSrc = "${projectDir}/src/main/kotlin"

                sourceDirectories.setFrom(files(listOf(mainJavaSrc, mainKotlinSrc)))
                classDirectories.setFrom(files(listOf(kotlinTree, javacTree)))
                executionData.setFrom(
                    fileTree(
                        mapOf(
                            "dir" to buildDir,
                            "includes" to listOf(
                                "jacoco/test${testVariant.capitalizeFirstChar()}UnitTest.exec",
                                "outputs/unit_test_code_coverage/${testVariant}UnitTest/test${testVariant.capitalizeFirstChar()}UnitTest.exec",
                                "outputs/code-coverage/connected/*coverage.ec"
                            )
                        )
                    )
                )
            }
        }
    }

    // define aggregating task
    project.plugins.apply("jacoco")
    project.extensions.configure(JacocoPluginExtension::class.java) {
        this.toolVersion = "0.8.7"
    }
    project.tasks.create("jacocoFullReport", JacocoReport::class.java) {
        group = "Reporting"
        description = "Generates an aggregate report from all subprojects"

        val jacocoReportTasks = coveredProjects.map { it.tasks.getByName("jacocoTestReport") as JacocoReport }
        setDependsOn(jacocoReportTasks)
        val sources = project.files(jacocoReportTasks.map { it.sourceDirectories }.flatten())

        additionalSourceDirs.setFrom(sources)
        sourceDirectories.setFrom(sources)

        classDirectories.setFrom(project.files(jacocoReportTasks.map { it.classDirectories }))
        executionData.setFrom(project.files(jacocoReportTasks.map { it.executionData }))

        reports {
            html.required.set(true)
            html.outputLocation.set(project.file("build/reports/jacoco/html"))
        }
    }
}

fun ExtraPropertiesExtension.getIfExists(name: String): Any? {
    return if (has(name)) get(name) else null
}
