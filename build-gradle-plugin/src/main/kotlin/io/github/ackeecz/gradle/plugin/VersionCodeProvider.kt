package io.github.ackeecz.gradle.plugin

import org.gradle.api.Project
import org.slf4j.LoggerFactory

class VersionCodeProvider(private val project: Project) {

    private val logger = LoggerFactory.getLogger("get-version-code")

    fun getVersionCode(): Int {
        return getCIEnvVersionCode() ?: getGitVersionCode()
    }

    private fun getCIEnvVersionCode(): Int? {
        return if (System.getenv(CI_ENV_VERSION_CODE) != null) {
            try {
                System.getenv(CI_ENV_VERSION_CODE).trim().toInt()
            } catch (e: Exception) {
                logger.warn("Exception while parsing CI_VERSION_CODE", e)
                null
            }
        } else {
            null
        }
    }

    private fun getGitVersionCode(): Int {
        val version = project.providers.exec {
            commandLine("git", "rev-list", "HEAD", "--count")
        }.standardOutput.asText.get()
        return version.trim().toInt()
    }

    companion object {

        private const val CI_ENV_VERSION_CODE = "CI_VERSION_CODE"
    }
}
