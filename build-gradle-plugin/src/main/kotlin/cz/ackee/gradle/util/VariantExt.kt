package cz.ackee.gradle.util

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.configurationcache.extensions.capitalized

/**
 * TODO: Replace this with some more official solution how to get assemble task from variant,
 *  but for now I wasn't able to find it.
 */
fun ApplicationVariant.assembleTask(project: Project, onAssembleTaskRegister: (Task) -> Unit) {
    project.tasks.whenTaskAdded {
        val variantName = this@assembleTask.name.capitalized()
        if (name == "assemble$variantName") {
            onAssembleTaskRegister(this)
        }
    }
}
