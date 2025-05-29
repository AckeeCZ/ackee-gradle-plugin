package io.github.ackeecz.gradle

import org.gradle.api.Project

/**
 * Defines location of a properties file.
 *
 * Only fileName needs to be specified. The file is, by default, searched for in the the
 * project's root directory.
 */
open class PropertiesExtensionKotlin(project: Project, fileName: String) {

    var fileName: String = fileName
    var path: String = "${project.rootDir}\\"
    
    val fullPath: String
        get() = path + fileName

}
