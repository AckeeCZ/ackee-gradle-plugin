package io.github.ackeecz.gradle.type

sealed class CustomBuildType {

    abstract val name: String
    abstract val appIdSuffix: String?
    abstract val appNameSuffix: String?
    abstract val signingConfigName: String?
    abstract val isMinifyEnabled: Boolean
    abstract val isShrinkResources: Boolean

    val enableProguard: Boolean
        get() = isMinifyEnabled || isShrinkResources

    object Debug : CustomBuildType() {

        override val name = "debug"
        override val appIdSuffix = ".debug"
        override val appNameSuffix = " D"
        override val signingConfigName = null
        override val isMinifyEnabled = false
        override val isShrinkResources = false
    }

    class Beta(versionCode: Int) : CustomBuildType() {

        override val name = "beta"
        override val appIdSuffix = ".beta"
        override val appNameSuffix = " B $versionCode"
        override val signingConfigName = "debug"
        override val isMinifyEnabled = true
        override val isShrinkResources = true
    }

    object Release : CustomBuildType() {

        override val name = "release"
        override val appIdSuffix = null
        override val appNameSuffix = null
        override val signingConfigName = "release"
        override val isMinifyEnabled = true
        override val isShrinkResources = true
    }
}
