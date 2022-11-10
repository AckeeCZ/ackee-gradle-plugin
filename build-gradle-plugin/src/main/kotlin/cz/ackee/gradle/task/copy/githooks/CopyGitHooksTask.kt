package cz.ackee.gradle.task.copy.githooks

import cz.ackee.gradle.task.Groups
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

object CopyGitHooksTask {

    private const val taskName = "copyGitHooks"

    fun registerTask(project: Project): TaskProvider<Copy> {
        return project.tasks.register<Copy>(taskName) {
            from("${project.rootDir}/.githooks")
            into("${project.rootDir}/.git/hooks")

            group = Groups.VERIFICATION
        }
    }
}
