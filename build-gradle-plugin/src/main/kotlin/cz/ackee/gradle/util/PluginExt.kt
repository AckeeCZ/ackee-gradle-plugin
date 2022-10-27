package cz.ackee.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import org.gradle.api.Project

// TODO remove this and use variants API
fun Project.getAndroidAppExtension(): AppExtension? {
    return project.extensions.findByType(AppExtension::class.java)
}

// TODO remove this and use variants API
fun Project.getAndroidAppExtensionOrThrow(): AppExtension {
    return getAndroidAppExtension() ?: throw Exception(
        "Not an Android application. Did you forget to apply 'com.android.application' plugin?"
    )
}

fun Project.getAndroidComponents(): AndroidComponentsExtension<*, *, *> = project.extensions.getByType(AndroidComponentsExtension::class.java)
