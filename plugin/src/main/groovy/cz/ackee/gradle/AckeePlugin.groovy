package cz.ackee.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

class AckeePlugin implements Plugin<Project> {

    static int getGitCommitsCount(Project project) {
        def stdout = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'rev-list', 'HEAD', '--count'
            standardOutput = stdout
        }
        return Integer.parseInt(stdout.toString().trim())
    }

    @Override
    void apply(Project project) {

        AppExtension android = project.extensions.findByType(AppExtension)
        if (!android) {
            throw new Exception("Not an Android application. " +
                    "Did you forget to apply `'com.android.application` plugin?")
        }

        /**
         * Define properties with keystore info
         */
        def keystorePropertiesExt = project.extensions.create('keystoreProperties', PropertiesExtension, project)
        keystorePropertiesExt.fileName = "keystore.properties"
        def keystoreProperties = new Properties()
        keystoreProperties.load(project.file(keystorePropertiesExt.fullPath()).newReader())
        project.ext.keystoreProperties = keystoreProperties

        /**
         * Define properties with application info
         */
        def appPropertiesExt = project.extensions.create('appProperties', PropertiesExtension, project)
        appPropertiesExt.fileName = "app.properties"
        def appProperties = new Properties()
        appProperties.load(project.file(appPropertiesExt.fullPath()).newReader())
        project.ext.appProperties = appProperties

        /**
         * Get count of git commits
         */
        project.ext.gitCommitsCount = getGitCommitsCount(project)
        project.ext.getGitCommitsCount = { getGitCommitsCount(project) }

        project.afterEvaluate {

            /**
             * Set output apk destination to file App.apk in outputs folder in project root
             */
            android.applicationVariants.all { variant ->
                def outputs = new File(project.rootDir, "outputs")
                outputs.mkdir()
                def apkFile = new File(outputs, "App.apk")

                variant.outputs.all { output ->
                    def taskName = "copyAndRename${variant.name.capitalize()}APK"
                    Task copyAndRenameAPKTask = project.task(taskName, type: Copy) {
                        from output.outputFile.getParent()
                        into outputs
                        include output.outputFileName
                        rename(output.outputFileName, apkFile.getName())
                    }

                    // if copyAndRenameAPKTask needs to automatically execute assemble before
                    copyAndRenameAPKTask.dependsOn(variant.assemble)
                    copyAndRenameAPKTask.mustRunAfter(variant.assemble)

                    // if assemble needs to automatically execute copyAndRenameAPKTask after
                    variant.assemble.finalizedBy(copyAndRenameAPKTask)
                }
            }

            /**
             * Copy mapping.txt from its location to outputs folder in project root
             */
            android.applicationVariants.all { variant ->
                if (variant.getBuildType().isMinifyEnabled()) {
                    variant.assemble.doLast {
                        project.copy {
                            from variant.mappingFile
                            into "${project.rootDir}/outputs"
                        }
                    }
                }
            }
        }

        /**
         * Defines standard signing configs for debugging and release.
         * Keystores must be located in keystore directory in project's root directory.
         */
        android.signingConfigs {
            def keystoreDir = new File(project.rootDir, "keystore")

            release {
                keyAlias keystoreProperties['key_alias']
                storeFile new File(keystoreDir, keystoreProperties['key_file'])
                storePassword keystoreProperties['key_password']
                keyPassword keystoreProperties['key_password']
            }

            debug {
                keyAlias "androiddebugkey"
                storeFile new File(keystoreDir, "debug.keystore")
                storePassword 'android'
                keyPassword 'android'
            }
        }

        /**
         * Defines standard build types: Debug, Beta and Release.
         * **Debug** type should be used only during development
         * **Beta** is used for internal testing
         * **Release** is used in production
         */
        android.buildTypes {
            debug {
                applicationIdSuffix '.debug'
                manifestPlaceholders = [
                        appId        : appProperties['package_name'] + applicationIdSuffix,
                        appNameSuffix: " D"
                ]
            }

            beta {
                applicationIdSuffix '.beta'
                manifestPlaceholders = [
                        appId        : appProperties['package_name'] + applicationIdSuffix,
                        appNameSuffix: " B " + project.android.defaultConfig.versionCode
                ]

                signingConfig project.android.signingConfigs.debug
                minifyEnabled true
                proguardFiles project.android.getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            }

            release {
                signingConfig project.android.signingConfigs.release
                minifyEnabled true
                proguardFiles project.android.getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            }
        }
    }
}
