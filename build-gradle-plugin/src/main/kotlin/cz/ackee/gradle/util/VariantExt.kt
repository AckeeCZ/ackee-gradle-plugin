package cz.ackee.gradle.util

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.configurationcache.extensions.capitalized

/**
 * TODO: Replace this with `wiredWith` function once possible AGP bug fixed.
 *  Currently `SingleArtifact.APK`provides incorrect output path.
 */
fun ApplicationVariant.assembleTask(project: Project, onAssembleTaskRegister: (Task) -> Unit) {
    project.tasks.whenTaskAdded {
        val variantName = this@assembleTask.name.capitalized()
        if (name == "assemble$variantName") {
            onAssembleTaskRegister(this)
        }
    }
}

/**
 * TODO: Replace this with `wiredWith` function once possible AGP bug fixed.
 *  Currently `SingleArtifact.BUNDLE`provides incorrect output path.
 */
fun ApplicationVariant.bundleTask(project: Project, onAssembleTaskRegister: (Task) -> Unit) {
    project.tasks.whenTaskAdded {
        val variantName = this@bundleTask.name.capitalized()
        if (name == "bundle$variantName") {
            onAssembleTaskRegister(this)
        }
    }
}
