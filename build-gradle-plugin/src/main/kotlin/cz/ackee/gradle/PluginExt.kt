package cz.ackee.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.Project

fun Project.getAndroidAppExtension(): AppExtension? {
    return project.extensions.findByType(AppExtension::class.java)
}

fun Project.getAndroidAppExtensionOrThrow(): AppExtension {
    return getAndroidAppExtension() ?: throw Exception(
        "Not an Android application. Did you forget to apply 'com.android.application' plugin?"
    )
}
