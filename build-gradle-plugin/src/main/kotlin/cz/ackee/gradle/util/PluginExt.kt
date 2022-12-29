package cz.ackee.gradle.util

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.Properties

fun Project.getApplicationAndroidComponents(): ApplicationAndroidComponentsExtension {
    return project.extensions.getByType()
}

fun Properties.loadPropertiesFile(file: File): Properties {
    val fileReader = FileReader(file)
    val bufferedReader = BufferedReader(fileReader)
    load(bufferedReader)
    return this
}
