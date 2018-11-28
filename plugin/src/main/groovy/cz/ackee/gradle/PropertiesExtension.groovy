package cz.ackee.gradle

import org.gradle.api.Project

/**
 * Defines location of a properties file.
 *
 * Only fileName needs to be specified. The file is, by default, searched for in the the
 * project's root directory.
 */
class PropertiesExtension {
    String fileName
    String path

    PropertiesExtension(Project project) {
        path = "${project.rootDir}\\"
    }

    String fullPath() {
        return path + fileName
    }
}
