package io.github.ackeecz.gradle.util

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

/**
 * TODO: Replace this with `wiredWith` function once possible AGP bug fixed.
 *  Currently `SingleArtifact.APK`provides incorrect output path.
 */
fun ApplicationVariant.assembleTask(project: Project, onAssembleTaskConfiguration: (Task) -> Unit) {
    val variantName = this@assembleTask.name.uppercaseFirstChar()
    project.tasks.matching { it.name == "assemble$variantName" }
        .configureEach { onAssembleTaskConfiguration(this) }
}

/**
 * TODO: Replace this with `wiredWith` function once possible AGP bug fixed.
 *  Currently `SingleArtifact.BUNDLE`provides incorrect output path.
 */
fun ApplicationVariant.bundleTask(project: Project, onBundleTaskConfiguration: (Task) -> Unit) {
    val variantName = this@bundleTask.name.uppercaseFirstChar()
    project.tasks.matching { it.name == "bundle$variantName" }
        .configureEach { onBundleTaskConfiguration(this) }
}
