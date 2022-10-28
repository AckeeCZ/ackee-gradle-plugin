package cz.ackee.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.Properties

fun Project.getApplicationAndroidComponents(): ApplicationAndroidComponentsExtension =
    project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

fun Properties.loadPropertiesFile(file: File): Properties {
    val fileReader = FileReader(file)
    val bufferedReader = BufferedReader(fileReader)
    load(bufferedReader)
    return this
}
