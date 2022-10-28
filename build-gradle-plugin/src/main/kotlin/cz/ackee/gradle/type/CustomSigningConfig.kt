package cz.ackee.gradle.type

import java.io.File

sealed class CustomSigningConfig {

    abstract val name: String
    abstract val keyAlias: String?
    abstract val storePassword: String?
    abstract val keyPassword: String?
    abstract val storeFile: File?

    class Debug(keystoreDir: File) : CustomSigningConfig() {

        override val name = "debug"
        override val keyAlias = "androiddebugkey"
        override val storePassword = "android"
        override val keyPassword = "android"
        override val storeFile = File(keystoreDir, "debug.keystore")
    }

    class Release(
        keystoreDir: File,
        storeFileName: String,
        override val storePassword: String?,
        override val keyAlias: String?,
        override val keyPassword: String?
    ) : CustomSigningConfig() {

        override val name = "release"
        override val storeFile = File(keystoreDir, storeFileName)
    }
}
