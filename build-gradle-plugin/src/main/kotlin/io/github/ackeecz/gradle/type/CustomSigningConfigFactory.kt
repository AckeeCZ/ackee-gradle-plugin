package io.github.ackeecz.gradle.type

import org.gradle.api.Project
import java.io.File
import java.util.Properties

class CustomSigningConfigFactory(
    private val project: Project,
    private val keystoreProperties: Properties
) {

    fun createSigningConfigs(): List<CustomSigningConfig> {
        val keystoreDir = File(project.rootDir, "keystore")
        return listOf(
            CustomSigningConfig.Debug(keystoreDir),
            CustomSigningConfig.Release(
                keystoreDir = keystoreDir,
                storeFileName = keystoreProperties["key_file"] as String? ?: "",
                storePassword = keystoreProperties["key_password"] as String?,
                keyAlias = keystoreProperties["key_alias"] as String?,
                keyPassword = keystoreProperties["key_password"] as String?
            )
        )
    }
}
