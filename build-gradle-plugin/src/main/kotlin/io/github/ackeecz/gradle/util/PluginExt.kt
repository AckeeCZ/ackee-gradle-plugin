package io.github.ackeecz.gradle.util

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.io.StringReader
import java.util.Properties

fun Project.getApplicationAndroidComponents(): ApplicationAndroidComponentsExtension {
    return project.extensions.getByType()
}

/**
 * Loads [file] as [Properties], or returns empty [Properties] if the file does not exist.
 */
fun Project.loadProperties(file: File): Properties {
    val content = providers.fileContents(layout.projectDirectory.file(file.absolutePath)).asText.orNull
    return Properties().apply {
        if (content != null) {
            load(StringReader(content))
        }
    }
}
