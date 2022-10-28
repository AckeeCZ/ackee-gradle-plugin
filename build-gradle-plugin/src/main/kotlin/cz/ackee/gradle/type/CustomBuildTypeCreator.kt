package cz.ackee.gradle.type

import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.ApplicationBuildType
import org.gradle.api.NamedDomainObjectContainer

class CustomBuildTypeCreator(
    private val buildTypeContainer: NamedDomainObjectContainer<out ApplicationBuildType>,
    private val signingConfigContainer: NamedDomainObjectContainer<out ApkSigningConfig>
) {

    fun maybeCreate(customBuildType: CustomBuildType) {
        buildTypeContainer.maybeCreate(customBuildType.name).apply {
            applicationIdSuffix = customBuildType.appIdSuffix
            customBuildType.appNameSuffix?.let { manifestPlaceholders += mapOf("appNameSuffix" to it) }
            customBuildType.signingConfigName?.let { signingConfig = signingConfigContainer.getByName(it) }
            isMinifyEnabled = customBuildType.isMinifyEnabled
            isShrinkResources = customBuildType.isShrinkResources

            if (customBuildType.enableProguard) {
                proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
            }
        }
    }
}
