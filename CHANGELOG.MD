# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [4.0.0] - 2026-10-08
### Removed
- **Breaking change**: Removed JaCoCo code coverage support. The `jacocoTestReport` and `jacocoFullReport`
tasks are no longer registered and the `jacocoExcludedProjects`, `jacocoTestVariant` and `jacocoExcludedFiles`
`ext` properties are ignored. When updating, consuming projects should:
  - delete the dead `gradle/jacoco-config.gradle` file and its `apply from: "$rootDir/gradle/jacoco-config.gradle"`
  line in the root build script,
  - update Ackee GitLab CI components to at least `6.2.0`, because older versions of the `unit_tests` component
  run `jacocoFullReport` whenever `gradle/jacoco-config.gradle` exists.

## [3.5.1] - 2026-10-08
### Fixed
- Changing the content of `app.properties` or `keystore.properties` now invalidates the configuration
cache. Previously, the cached configuration with the old values (e.g. `version_name` or signing
config) was reused until something else invalidated the cache.

## [3.5.0] - 2026-09-21
### Changed
- Update to AGP 9.4.1 and adapt the plugin to AGP 9 (new DSL, built-in Kotlin). Consuming projects
must be migrated to AGP 9 and drop the `org.jetbrains.kotlin.android` plugin.
- Update Gradle to 9.7.1, Jacoco to 0.8.15, Dokka to 2.2.0 and the Maven publish plugin to 0.37.0.
- Annotate copy/check tasks with `@DisableCachingByDefault` and declare path sensitivity of their
inputs, which removes the Gradle cacheability warnings.
- Replace the deprecated `by extra(...)` delegate syntax with `extra.set(...)`, which is required by
Gradle 10.

## [3.4.0] - 2025-09-16
### Removed
- FetchDetektConfigTask because it is replaced by a new approach of Detekt config sharing (for more
info see https://github.com/AckeeCZ/detekt-config)

## [3.3.2] - 2025-09-13
### Changed
- Add verification of source and target folders in CopyGitHooksTask and register the task only if both 
folders exist.

## [3.3.1] - 2025-09-11
### Changed
- Update all dependencies including old Jacoco version that was problematic since Gradle 9.0.0

## [3.3.0] - 2025-05-27
### Removed
- Support for Gradle plugins for checking outdated dependencies and vulnerable dependencies. The
existing integration on CI using Danger was not convenient and we have now other process for updating
dependencies. Updating dependencies does maximum for fixing vulnerabilities as well.

## [3.2.0] - 2024-03-05
### Added
- Support for Gradle plugins for checking outdated dependencies and vulnerable dependencies. This 
version is compatible with Danger plugin https://github.com/AckeeCZ/danger-kotlin-dependencies-check
of version 1.0.0. Newly added Gradle plugins are:
  * https://github.com/jeremylong/DependencyCheck
  * https://github.com/ben-manes/gradle-versions-plugin

## [3.1.6] - 2023-09-07

### Fixed

- Fix unnecessary dependency between `copyMappingFile` task and `checkChagelogTask`

## [3.1.5] - 2023-09-01

### Fixed

- `CheckChangelogFileTask` now supports configuration cache too

## [3.1.4] - 2023-09-01

### Changed

- Update all dependencies to the latest where possible

## [3.1.3] - 2023-07-31

### Fixed

- Fix external execution to support configuration cache
- Fix getting AGP tasks
  - Tasks should be configured via `configureEach` instead of using `whenTaskAdded`

## [3.1.2] - 2023-06-23

### Fixed
- Applying default proguard file
  - Proguard file `proguard-android-optimize.txt` is now referenced via `getDefaultProguardFile`
    and being applied correctly.

## [3.1.1] - 2023-05-14

### Fixed
- Copying artifacts
  - Copying tasks are now added to the task graph via `finalizedBy` instead of `wiredWith`, 
    which previously caused errors during building. This issue was likely caused 
    by some AGP bug, as the output file provided to the task by AGP did not match 
    the expected input path for the subsequent task.

## [3.1.0] - 2023-05-10

### Added
- Support for Gradle 8

### Changed
- Update all dependencies to the latest where possible

## [3.0.0] - 2022-12-29
- Plugin rewrite to the new Android gradle plugin API
- Remove running lint on each non-debug build
  - It causes slow builds and lint should run only on CI

### Changed
- New plugin ids (check [README.md] file)

## [2.3.1] - 2022-09-16

### Fixed
- Add new jacoco execution data path 
- Keystore properties not properly loaded.

## [2.3.0]

### Added

- Prevents release builds from failing when there is no `keystore.properties` file in the project.  
Release builds can still be built locally if you define a local `keystore.properties` file like:  
```
# keystore.properties
# keystore file should be located in a dir named keystore/ in the project root
#   E.G. MyApp/keystore/debug.keystore
key_alias=[project]
key_password=[keystore password]
key_file=[keystore file name] # E.G. debug.keystore
```

## [2.2.0] - 2021-12-16

### Added

- Add common settings of lint
- Add dependency to lint to non-debuggable build types assemble tasks

## [2.1.0] - 2021-08-25

### Added

- Update dependencies
- Copy test apk to the `outputs` folder

## [2.0.4] - 2021-05-10

### Changed

- Beta builds enable resource shrinking
- Beta builds use proguard-android-optimize.txt as release builds do. It should not have effect on R8 though.

## [2.0.3] - 2021-05-07
### Fixed
- Set JaCoCo 0.8.7 also for `jacocoFullReport` task

## [2.0.2] - 2021-05-06
### Changed
- Updated JaCoCo version to 0.8.7, which is needed for Kotlin 1.5 to work

## [2.0.1] - 2021-04-08
### Changed
- Adjust the way githooks copy task is registered

## [2.0.0] - 2021-03-29
### Changed
- Migrate from jCenter to Maven Central 🎉
- ‼️ Important ‼️ groupId has changed. New groupId is `io.github.ackeecz`.
