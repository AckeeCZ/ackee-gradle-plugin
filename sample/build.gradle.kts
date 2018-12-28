buildscript {
    val kotlinVersion by extra("1.3.10")

    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/ackeecz/gradle-plugin")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("cz.ackee:build-gradle-plugin:1.0.0-RC13")
    }
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

val clean = task<Delete>("clean") {
    delete(rootProject.buildDir)
}
