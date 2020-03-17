[ ![Download](https://api.bintray.com/packages/ackeecz/gradle-plugin/build-gradle-plugin/images/download.svg) ](https://bintray.com/ackeecz/gradle-plugin/build-gradle-plugin/_latestVersion)
# Ackee gradle plugin

The plugin configures gradle project in several ways:

### Build types
Each and every project made in Ackee declares 3 build types:
**Debug** (for fast builds during development),
**Beta** (for internal testing) and
**Release** (for play store).

Applying the plugin automatically declare all three build types and initialize them with
useful suffixes attached to their names:
 - **debug** version get `D` suffix attached to its name such as `MyApp D`
 - **beta** version get `B` with versionCode attached, such as `MyApp B 171`
 - **release** version is displayed simply such as `MyApp`

**Debug** and **Beta** versions receive `.debug` and `.beta` suffixes attached to their application
ids to prevent collision between multiple installations of the same app with different variants on
the same device.

**Beta** and **Release** versions are build with proguard enabled.

You have to use custom `AndroidManifest.xml` for each build variant.
For debug and beta variants, use custom application label:
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

- **Debug** config is used to sign **Beta** build types with a debug keystore that does not allow
upload to play store.
- **Release** config is used to sign **Relase** build types that allows upload to play store.

Credentials used for release signing should be defined in a file with following properties:
```
key_alias=myapp
key_file=release.keystore
key_password=mypass
```

By default the credentials are extracted from `keystore.properties` file located in the project's
root directory. You can change the location by declaring custom path and/or fileName such as:

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
By default properties are extracted from `app.properties` file located in the project's
root directory but file location can be changed with
```
appProperties {
    path = "${project.rootDir}\\" // optional
    fileName = "custom.properties" // optional
}
```

### Git hooks copying
If folder `.githooks` exists within the repository content of this folder is copied to the `.git/hooks` folder. Thats because we want to have some
hooks as part of the git repository and there is no standard way to do so.  


## Usage

It can be used in any (Android) project by adding classpath dependency into project's buildscript:
```
buildscript {
    dependencies {
        classpath("cz.ackee:build-gradle-plugin:1.0.0")
    }
}
```

and applying the plugin in the app's module:

```
plugins {
    id("cz.ackee.build")
}
```

App's `keystore.properties` and `app.properties` locations can be changed using
`keystoreProperties` and `appProperties` such as:
```
appProperties {
    path = "${project.rootDir}\\" // optional
    fileName = "custom.properties" // optional
}
```

You don't have to declare 3 default build types (**Debug**, **Beta**, **Release**) as they are
already set up by the plugin. You can still add another build type (for example **Monkey**) as
normal and ignore the 3 default ones. If you want to add an extra configuration to each build type
(e.g. hockeyApp ID for each type), you can declare the types with the extra configuration and
the plugin will still work.

## Editing
The project can be opened and edited directly in Android Studio.
You will have to use Sync Project with Gradle Files button a lot though.
The project does not sync automatically after opening the project and sometimes when
making changes to the plugin.

## Testing
After you make some changes it is a good idea to test that. You can run a `publishToMavenLocal`
gradle task which will publish your updated plugin to the local maven repository on your machine.
Then you can easily test that in some of your Android projects. All you need to do is include your
updated plugin the standard way and add `mavenLocal()` repository to your `buildscript`
`repositories` in the `build.gradle` of your root project folder.

It is also recommended to increase a version of your plugin before publishing to the local maven
repository. This way you can be totally sure that you use your updated plugin and not the old version
fetched from the central maven repository.

## Publishing
Plugins can be published to specialized Gradle repository https://plugins.gradle.org/.
Unfortunately there were some problems syncing the project when the plugin was retrieved
from Gradle repository. To work around the issue, the plugin is now uploaded to bintray
and jcenter.

Publishing process to bintray is the same as for any standard library.
