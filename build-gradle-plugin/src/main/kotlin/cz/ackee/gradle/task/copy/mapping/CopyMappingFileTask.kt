package cz.ackee.gradle.task.copy.mapping

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import cz.ackee.gradle.task.Groups
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import java.io.File

object CopyMappingFileTask {

    private const val taskName = "CopyMappingFile"

    fun registerTask(
        project: Project,
        variant: Variant,
        output: File,
    ): TaskProvider<Copy> {
        return project.tasks.register(createTaskName(variant), Copy::class.java) {
            val aabFile = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE).orNull?.asFile

            from(aabFile)
            into(output)
            group = Groups.WIP
        }
    }

    private fun createTaskName(variant: Variant) = "${variant.name}$taskName"
}
