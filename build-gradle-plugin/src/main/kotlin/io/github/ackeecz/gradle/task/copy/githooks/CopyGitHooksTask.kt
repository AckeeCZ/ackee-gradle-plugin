package io.github.ackeecz.gradle.task.copy.githooks

import io.github.ackeecz.gradle.task.Groups
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.configuration.problems.logger
import org.gradle.kotlin.dsl.register
import java.io.File

object CopyGitHooksTask {

    private const val taskName = "copyGitHooks"

    fun registerTask(project: Project): TaskProvider<Copy>? {
        val sourceFolderPath = "${project.rootDir}/.githooks"
        val targetFolderPath = "${project.rootDir}/.git/hooks"

        if (!File(sourceFolderPath).isDirectory ||
            !File(targetFolderPath).isDirectory
        ) {
            logger.warn("Invalid copy git hooks folder paths. Skipping git hooks installation.")
            return null
        }

        return project.tasks.register<Copy>(taskName) {
            from(sourceFolderPath)
            into(targetFolderPath)

            group = Groups.VERIFICATION
        }
    }
}
