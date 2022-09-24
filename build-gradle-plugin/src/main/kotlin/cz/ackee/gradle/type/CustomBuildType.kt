package cz.ackee.gradle.type

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.NamedDomainObjectContainer

sealed class CustomBuildType {

    abstract val name: String
    abstract val appIdSuffix: String?
    abstract val appNameSuffix: String?
    abstract val signingConfigName: String?
    abstract val isMinifyEnabled: Boolean
    abstract val isShrinkResources: Boolean
    abstract val enableProguard: Boolean

    fun maybeCreate(container: NamedDomainObjectContainer<BuildType>, android: AppExtension) {
        container.maybeCreate(name).apply {
            applicationIdSuffix = appIdSuffix
            appNameSuffix?.let { manifestPlaceholders += mapOf("appNameSuffix" to it) }
            signingConfigName?.let { signingConfig = android.signingConfigs.getByName(it) }
            isMinifyEnabled = this@CustomBuildType.isMinifyEnabled
            isShrinkResources = this@CustomBuildType.isShrinkResources

            if (enableProguard) {
                proguardFiles(android.getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
    }

    object Debug : CustomBuildType() {

        override val name = "debug"
        override val appIdSuffix = ".debug"
        override val appNameSuffix = " D"
        override val signingConfigName = null
        override val isMinifyEnabled = false
        override val isShrinkResources = false
        override val enableProguard = false
    }

    class Beta(versionCode: Int) : CustomBuildType() {

        override val name = "beta"
        override val appIdSuffix = ".beta"
        override val appNameSuffix = " B $versionCode"
        override val signingConfigName = "debug"
        override val isMinifyEnabled = true
        override val isShrinkResources = true
        override val enableProguard = true
    }

    object Release : CustomBuildType() {

        override val name = "release"
        override val appIdSuffix = null
        override val appNameSuffix = null
        override val signingConfigName = "release"
        override val isMinifyEnabled = true
        override val isShrinkResources = true
        override val enableProguard = true
    }
}
