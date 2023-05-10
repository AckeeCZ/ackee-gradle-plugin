[ ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/build-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/build-gradle-plugin)
# Ackee gradle plugin

The plugin configures gradle project in several ways:

### Build types

Each and every project made in Ackee declares 3 build types:
**Debug** (for fast builds during development),
**Beta** (for internal testing) and
**Release** (for play store).

Applying the plugin automatically declare all three build types and initialize them with useful suffixes attached to
their names:

- **debug** version get `D` suffix attached to its name such as `MyApp D`
- **beta** version get `B` with versionCode attached, such as `MyApp B 171`
- **release** version is displayed simply such as `MyApp`

**Debug** and **Beta** versions receive `.debug` and `.beta` suffixes attached to their application ids to prevent
collision between multiple installations of the same app with different variants on the same device.

**Beta** and **Release** versions are build with proguard enabled.

You have to use custom `AndroidManifest.xml` for each build variant. For debug and beta variants, use custom application
label:

```
<application
    android:label="${appName}${appNameSuffix}"
    tools:replace="android:label"/>
```

For release version you can retrieve the name directly from strings:

```
<application
    android:label="@string/app_name"
    tools:replace="android:label"/>
```

Check `sample` subproject for sample implementation.

### Copy apk and mapping

The plugin configures the project to automatically copy assembled apk, app bundle and mapping files into
`outputs` directory in the project's root after each successful build.

### Signing configs

The plugin also defines two default signing configs: **Debug** and **Release**

- **Debug** config is used to sign **Beta** build types with a debug keystore that does not allow upload to play store.
- **Release** config is used to sign **Release** build types that allows upload to play store.

Credentials used for release signing should be defined in a file with following properties:

```
key_alias=myapp
key_file=release.keystore
key_password=mypass
```

By default the credentials are extracted from `keystore.properties` file located in the project's root directory. You
can change the location by declaring custom path and/or fileName such as:

```
keystoreProperties {
    path = "${project.rootDir}\\keystore\\" // optional
    fileName = "release.keystore.properties" // optional
}
```

in the app's module.

### App properties

Similarly to `keystore.properties` file used for signing configs, the plugin expects an
`app.properties` file with at least `package_name` defined:

```
package_name=cz.ackee.myapp
version_name=1.0.0
some_key=xxxxxxxxxxxxxxxx
...
```

All properties also become accessible in the build scripts like:

```
appProperties['package_name']
```

By default properties are extracted from `app.properties` file located in the project's root directory but file location
can be changed with

```
appProperties {
    path = "${project.rootDir}\\" // optional
    fileName = "custom.properties" // optional
}
```

### Git hooks copying

If folder `.githooks` exists within the repository content of this folder is copied to the `.git/hooks` folder. Thats
because we want to have some hooks as part of the git repository and there is no standard way to do so.

### JaCoCo code coverage support

For each module task `jacocoTestReport` is generated that generates html JaCoCo report. Also
`jacocoFullReport` task is added for the whole project that aggregates results of all submodules tasks and generates
html output to `build/reports/jacoco/html/index.html`. These configuration properties are present:

- `jacocoExcludedProjects` - string list of module names you want to exclude. Defaults to empty list.
- `jacocoTestVariant` - build variant of the test against which code coverage is computed. Defaults to `devApiDebug`
  since it's the most common setup on Ackee projects.
- `jacocoExcludedFiles` - string list of excluded file patterns that we don't want to include in code coverage report.
  Defaults to empty list.

Place your configuration file to `gradle/jacoco-config.gradle` in your project root.

Example configuration

```
project.ext {
    jacocoExcludedProjects = [
            "features",
            "libraries"
    ]

    jacocoTestVariant = "devApiDebug"

    jacocoExcludedFiles = [
            '**/*App.*',
            '**/*Application*',
            '**/*Activity*',
            '**/*Fragment*',
            '**/*View.*',
            '**/*ViewGroup.*',
            '**/*JsonAdapter.*',
            '**/*Layout*',
            '**/epoxy/**',
            '**/di/**',
            '**/*Dagger.*',
            '**/ui_components/**',
            "**/com/**",
            "**/androidx/**",
            "**/org/**",
            "**/BuildConfig.*",
            "**/*Model_.*",
            "**/*styleable*",
            "**/grpc/*",
            "**/*Exception*",
            "**DI**"
    ]
}
```

The last thing you need to do is to place to your root `build.gradle` this line at the end of the file so the ext
properties are loaded within the project

```
apply from: "$rootDir/gradle/jacoco-config.gradle"
```

### Fetch of common Detekt config

Ackee apps use [detekt](https://github.com/detekt/detekt) tool for static analysis of the Kotlin source code. We have
common configuration defined in our [styleguide](https://github.com/AckeeCZ/styleguide/tree/master/android) repository.
There is a `fetchDetektConfig` Gradle task that fetches newest config from the Github and stores it to the project
directory to a file `detekt-config-common.yml`.

## Usage

It can be used in any (Android) project by adding plugin in the project's `build.gradle` file:

``` groovy
plugins {
    id "cz.ackee.build" version "3.0.0" apply false
    
    // or
    
    id "cz.ackee.verifications" version "3.0.0" apply false
    id "cz.ackee.variants" version "3.0.0" apply false
    id "cz.ackee.deployment" version "3.0.0" apply false
    id "cz.ackee.config" version "3.0.0" apply false
}
```

Or you can use the old way: 

``` groovy
buildscript {
    dependencies {
        classpath("io.github.ackeecz:build-gradle-plugin:3.0.0")
    }
}
```

and applying the plugin in the app's module:

```
plugins {
    id("cz.ackee.build")
    
    // or
    
    id("cz.ackee.verifications")
    id("cz.ackee.variants")
    id("cz.ackee.deployment")
    id("cz.ackee.config")
}
```
- `cz.ackee.build` contains everything
- `cz.ackee.verifications` sets up detekt, code coverage, lint, copying git hooks
- `cz.ackee.variants` configures build types and signing
- `cz.ackee.deployment` copies artifacts and checks changelog
- `cz.ackee.config` provides `app.properties` and sets `versionCode`

App's `keystore.properties` and `app.properties` locations can be changed using
`keystoreProperties` and `appProperties` such as:

```
appProperties {
    path = "${project.rootDir}\\" // optional
    fileName = "custom.properties" // optional
}
```

You don't have to declare 3 default build types (**Debug**, **Beta**, **Release**) as they are already set up by the
plugin. You can still add another build type (for example **Monkey**) as normal and ignore the 3 default ones. If you
want to add an extra configuration to each build type
(e.g. hockeyApp ID for each type), you can declare the types with the extra configuration and the plugin will still
work.

## Editing

The project can be opened and edited directly in Android Studio. You will have to use Sync Project with Gradle Files
button a lot though. The project does not sync automatically after opening the project and sometimes when making changes
to the plugin.

## Testing

After you make some changes it is a good idea to test that. You can run a `publishReleasePublicationToMavenLocal`
gradle task which will publish your updated plugin to the local maven repository on your machine. Then you can easily
test that in some of your Android projects. All you need to do is include your updated plugin the standard way and
add `mavenLocal()` repository to your `buildscript` `repositories` in the `build.gradle` of your root project folder.

It is also recommended to increase a version of your plugin before publishing to the local maven repository. This way
you can be totally sure that you use your updated plugin and not the old version fetched from the central maven
repository.

## Publishing

Plugin is stored on Maven Central repository. Publishing process is the same as for any other library
stored on Maven Central and the guide for that can be found on Ackee Android hub.
