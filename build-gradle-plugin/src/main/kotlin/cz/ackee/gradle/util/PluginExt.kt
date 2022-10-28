package cz.ackee.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project

fun Project.getApplicationAndroidComponents(): ApplicationAndroidComponentsExtension =
    project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
