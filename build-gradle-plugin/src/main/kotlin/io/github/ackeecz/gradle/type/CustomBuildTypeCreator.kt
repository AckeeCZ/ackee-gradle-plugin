package io.github.ackeecz.gradle.type

import com.android.build.api.dsl.ApplicationExtension

class CustomBuildTypeCreator(
    private val applicationExtension: ApplicationExtension,
) {

    private val buildTypeContainer = applicationExtension.buildTypes
    private val signingConfigContainer = applicationExtension.signingConfigs

    fun maybeCreate(customBuildType: CustomBuildType) {
        buildTypeContainer.maybeCreate(customBuildType.name).apply {
            applicationIdSuffix = customBuildType.appIdSuffix
            customBuildType.appNameSuffix?.let { manifestPlaceholders += mapOf("appNameSuffix" to it) }
            customBuildType.signingConfigName?.let {
                signingConfig = signingConfigContainer.getByName(it)
            }
            isMinifyEnabled = customBuildType.isMinifyEnabled
            isShrinkResources = customBuildType.isShrinkResources

            if (customBuildType.enableProguard) {
                proguardFiles(
                    applicationExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
}
