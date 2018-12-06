package cz.ackee.gradle

import org.gradle.api.Project

/**
 * Defines location of a properties file.
 *
 * Only fileName needs to be specified. The file is, by default, searched for in the the
 * project's root directory.
 */
open class PropertiesExtensionKotlin(project: Project, fileName: String) {

    val fileName: String = fileName
    val path: String = "${project.rootDir}\\"
    
    val fullPath: String
        get() = path + fileName

}
