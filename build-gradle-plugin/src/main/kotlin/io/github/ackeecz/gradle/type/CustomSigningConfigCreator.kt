package io.github.ackeecz.gradle.type

import com.android.build.api.dsl.ApkSigningConfig
import org.gradle.api.NamedDomainObjectContainer

class CustomSigningConfigCreator(
    private val signingConfigContainer: NamedDomainObjectContainer<out ApkSigningConfig>
) {

    fun maybeCreate(customSigningConfig: CustomSigningConfig) {
        signingConfigContainer.maybeCreate(customSigningConfig.name).apply {
            keyAlias = customSigningConfig.keyAlias
            storePassword = customSigningConfig.storePassword
            keyPassword = customSigningConfig.keyPassword
            storeFile = customSigningConfig.storeFile
        }
    }
}
