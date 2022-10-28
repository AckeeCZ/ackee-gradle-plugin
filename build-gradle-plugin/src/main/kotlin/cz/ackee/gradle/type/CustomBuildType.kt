package cz.ackee.gradle.type

sealed class CustomBuildType {

    abstract val name: String
    abstract val appIdSuffix: String?
    abstract val appNameSuffix: String?
    abstract val signingConfigName: String?
    abstract val isMinifyEnabled: Boolean
    abstract val isShrinkResources: Boolean
    abstract val enableProguard: Boolean

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
