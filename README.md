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

Note: You should use custom application label in your `AndroidManifest.xml` for the enhanced
application names to appear:
```
<application android:label="${appName}${appNameSuffix}" />
```

### Copy apk and mapping

The plugin configures the project to automatically copy assembled apk and mapping files into
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


## Usage

It can be used in any (Android) project by adding classpath dependency `cz.ackee.gradle.plugin` into
project's buildscript:
```
buildscript {
    dependencies {
        classpath("cz.ackee.gradle.plugin:1.0")
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
Unfortunately, you will have to use Sync Project with Gradle Files button a lot.
The project does not sync automatically after opening the project and sometimes when
making changes to the plugin.

## Publishing
To be able to publish the plugin, you need to add following two properties to your
`~/.gradle/gradle.properties` file:

```
gradle.publish.key=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
gradle.publish.secret=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

You can get the keys in two ways:
- Log into https://plugins.gradle.org/u/ackee and manually copy the keys from there
- Let gradle do it for you by running `plugin portal`->`login` task. It will ask you for 
login credentials and copy the keys automatically. Cool.

Also don't forget to update the version.