plugins {
    id("java-gradle-plugin")
    `kotlin-dsl`
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ackee.internal.publish)
}

dependencies {
    compileOnly(files(libs::class.java.superclass.protectionDomain.codeSource.location))
    compileOnly(libs.android.tools.build)
}

gradlePlugin {
    plugins {
        plugin(
            pluginIdSuffix = "build",
            className = "AckeeGradlePlugin",
        )
        plugin(
            pluginIdSuffix = "config",
            className = "ConfigureAppPlugin",
        )
        plugin(
            pluginIdSuffix = "deployment",
            className = "DeploymentPlugin",
        )
        plugin(
            pluginIdSuffix = "variants",
            className = "VariantsPlugin",
        )
        plugin(
            pluginIdSuffix = "verifications",
            className = "VerificationsPlugin",
        )
    }
}

fun NamedDomainObjectContainer<PluginDeclaration>.plugin(
    pluginIdSuffix: String,
    className: String,
) {
    val packageName = "io.github.ackeecz.gradle"
    val pluginId = "io.github.ackeecz.plugin.$pluginIdSuffix"
    register(pluginId) {
        id = pluginId
        implementationClass = "$packageName.plugin.$className"
    }
}
